package com.demo.service.serviceimpl;

import com.demo.client.CartClient;
import com.demo.client.InventoryClient;
import com.demo.dto.*;
import com.demo.entity.Order;
import com.demo.entity.OrderItem;
import com.demo.enums.OrderStatus;
import com.demo.enums.PaymentStatus;
import com.demo.enums.PickupStatus;
import com.demo.event.OrderPlacedEvent;
import com.demo.event.ReduceStock;
import com.demo.event.RestoreStock;
import com.demo.exception.OrderNotFoundException;
import com.demo.mapper.OrderMapper;
import com.demo.repository.OrderRepository;
import com.demo.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    //    private final KafkaTemplate<String, ReduceStock> reduceStockKafkaTemplate;
    private final OrderMapper orderMapper;

    @Override
    public OrderDto placeOrder(String userId, String userEmail, PlaceOrderRequest request) {

        // 1. Validate selected items not empty
        if (request.getSelectedItems() == null || request.getSelectedItems().isEmpty()) {
            throw new IllegalStateException(
                    "Please select at least one item to order.");
        }

        // 2. Fetch each selected cart item from Cart Service
        List<CartItemDto> selectedCartItems = request.getSelectedItems()
                .stream()
                .map(selected -> {
                    // fetch cart item from cart service
//                    CartItemDto cartItem = cartClient.getCartItem(userId,selected.getCartItemId());
                    CartItemDto cartItem = this.getCartItem(userId, selected.getCartItemId());

                    if (selected.getQuantity() > cartItem.getQuantity()) {
                        throw new IllegalArgumentException("Requested quantity " + selected.getQuantity()
                                + " exceeds cart quantity " + cartItem.getQuantity()
                                + " for product: " + cartItem.getProductName());
                    }

                    // override quantity with selected quantity
                    cartItem.setQuantity(selected.getQuantity());
                    return cartItem;
                }).collect(Collectors.toList());

        // 3. Check inventory for selected items
        selectedCartItems.forEach(item -> {
            boolean inStock = this.isInStock(
                    item.getProductId(), item.getQuantity());
            if (!inStock) {
                throw new IllegalArgumentException("Product out of stock: " + item.getProductName());
            }
        });

        // 4. Calculate amounts from selected items only
        double totalAmount = selectedCartItems.stream()
                .mapToDouble(item -> {
                    double price = item.getDiscountedPrice() > 0
                            ? item.getDiscountedPrice() : item.getPrice();
                    return price * item.getQuantity();
                }).sum();

        double discountAmount = selectedCartItems.stream()
                .mapToDouble(item ->
                        (item.getPrice() - item.getDiscountedPrice()) * item.getQuantity()).sum();
        // 5. Build order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .userEmail(userEmail)
                .orderStatus(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .pickupStatus(PickupStatus.WAITING)
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(totalAmount)
                .build();

        // 6. Add only selected items to order
        selectedCartItems.forEach(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .imageUrl(cartItem.getImageUrl())
                    .brand(cartItem.getBrand())
                    .price(cartItem.getPrice())
                    .discountedPrice(cartItem.getDiscountedPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            order.addItem(orderItem);
        });

        // 7. Save order
        Order savedOrder = orderRepository.save(order);

//        // 8. Reduce inventory for selected items only
//        selectedCartItems.forEach(item ->
//                inventoryClient.reduceStock(item.getProductId(), item.getQuantity()));

        // 9. Handle cart items after order:
        //    - if full quantity ordered → remove item from cart
        //    - if partial quantity ordered → update remaining quantity in cart
        request.getSelectedItems().forEach(selected -> {
            CartItemDto cartItem = cartClient.getCartItem(
                    userId, selected.getCartItemId());

            if (selected.getQuantity() >= cartItem.getQuantity()) {
                // full quantity ordered → remove from cart
                cartClient.removeCartItem(userId, selected.getCartItemId());
            }
            // partial quantity → cart item stays with original quantity
            // (user can order remaining later)
        });

        // 10. Publish Kafka event
        publishReduceStockEvent(selectedCartItems);
        publishOrderPlacedEvent(savedOrder);


        log.info("Order placed: {} for {} selected items",
                savedOrder.getOrderNumber(), selectedCartItems.size());

        return orderMapper.mapToOrderDto(savedOrder);

    }


    @Override
    public OrderDto getOrderByNumber(String orderNumber, String userId, boolean isAdmin) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderNumber));

        // user can only see their own order
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(
                    "Order not found: " + orderNumber);
        }
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    public Page<OrderDto> getMyOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::mapToOrderDto);
    }

    @Override
    public OrderDto cancelOrder(String orderNumber, String userId, String reason) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderNumber));


        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(
                    "Order not found: " + orderNumber);
        }

        if (order.getOrderStatus() == OrderStatus.COMPLETED ||
                        order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Order cannot be cancelled in status: "
                            + order.getOrderStatus());
        }

// 4. Update order status → CANCELLED
        order.setOrderStatus(OrderStatus.CANCELLED);

        // 5. Update pickup status → CANCELLED (not EXPIRED)
        order.setPickupStatus(PickupStatus.EXPIRED);

        // 6. Set cancellation details
        order.setCancellationReason(
                reason != null ? reason : "Cancelled by customer");
        order.setCancelledAt(LocalDateTime.now());

        publishRestoreStockEvent(order.getItems());

        log.info("Order cancelled: {}", orderNumber);
        return orderMapper.mapToOrderDto(orderRepository.save(order));

    }


    @Override
    public Page<OrderDto> getAllOrders(Pageable pageable) {

        return orderRepository.findAll(pageable)
                .map(orderMapper::mapToOrderDto);
    }

    @Override
    public OrderDto updateOrderStatus(String orderNumber, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderNumber));

        order.setOrderStatus(request.getOrderStatus());

        if (request.getOrderStatus() == OrderStatus.CANCELLED) {
            order.setCancellationReason(request.getCancellationReason());
            order.setCancelledAt(LocalDateTime.now());
            order.setPickupStatus(PickupStatus.EXPIRED);
        }

        if (request.getOrderStatus() == OrderStatus.COMPLETED) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPickedUpAt(LocalDateTime.now());
        }

        log.info("Order status updated: {} -> {}",
                orderNumber, request.getOrderStatus());
        return orderMapper.mapToOrderDto(orderRepository.save(order));
    }

    @Override
    public OrderDto updatePickupStatus(String orderNumber, PickupStatusUpdateRequest request) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderNumber));

        order.setPickupStatus(request.getPickupStatus());

        if (request.getPickupStatus() == PickupStatus.READY) {
            order.setReadyAt(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.READY);
        }

        if (request.getPickupStatus() == PickupStatus.PICKED_UP) {
            order.setPickedUpAt(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        if (request.getPickupStatus() == PickupStatus.EXPIRED) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());

            // restore inventory
            order.getItems().forEach(item ->
                    inventoryClient.reduceStock(
                            item.getProductId(), -item.getQuantity()));
        }

        log.info("Pickup status updated: {} -> {}",
                orderNumber, request.getPickupStatus());
        return orderMapper.mapToOrderDto(orderRepository.save(order));
    }

    @Override
    public Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByOrderStatus(status, pageable)
                .map(orderMapper::mapToOrderDto);
    }

    @Override
    public Page<OrderDto> getOrdersByPickupStatus(PickupStatus status, Pageable pageable) {
        return orderRepository.findByPickupStatus(status, pageable)
                .map(orderMapper::mapToOrderDto);
    }


    // ── Helpers ───────────────────────────────────────────

    private String generateOrderNumber() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        return "ORD-" + date + "-" + uuid;
    }

    private void publishOrderPlacedEvent(Order order) {
        List<OrderPlacedEvent.OrderItemEvent> itemEvents = order.getItems()
                .stream()
                .map(item -> OrderPlacedEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .items(itemEvents)
                .finalAmount(order.getFinalAmount())
                .build();

        kafkaTemplate.send("order-placed", event);
        log.info("Order placed event published: {}", order.getOrderNumber());
    }

    private void publishReduceStockEvent(List<CartItemDto> cartItemDtoList) {

        for (CartItemDto item : cartItemDtoList) {

            ReduceStock reduceStock = new ReduceStock();
            reduceStock.setProductId(item.getProductId());
            reduceStock.setQuantity(item.getQuantity());

            kafkaTemplate.send("REDUCE-STOCK", item.getProductId().toString(), reduceStock)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("❌ Failed to publish event for productId={}",
                                    reduceStock.getProductId(), ex);
                        } else {
                            log.info("✅ Event published | productId={} | partition={} | offset={}",
                                    reduceStock.getProductId(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        }
    }

    private void publishRestoreStockEvent(List<OrderItem> items) {

        for (OrderItem item : items) {

            RestoreStock restoreStock = new RestoreStock();
            restoreStock.setProductId(item.getProductId());
            restoreStock.setQuantity(item.getQuantity());

            kafkaTemplate.send("RESTORE-STOCK", item.getProductId().toString(), restoreStock)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("❌ Failed to publish event for productId={}",
                                    restoreStock.getProductId(), ex);
                        } else {
                            log.info("✅ Restore Event published | productId={} | partition={} | offset={}",
                                    restoreStock.getProductId(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        }
    }


    //-------------------------------------------------------------------------------------------------
    //circuit breakers

    @CircuitBreaker(name = "productService", fallbackMethod = "cartFallback")
    private CartItemDto getCartItem(String userId, Long selectedCartItemId) {
        CartItemDto cartItemDto = cartClient.getCartItem(userId, selectedCartItemId);
        return cartItemDto;
    }

    public CartItemDto cartFallback(String userId, Long selectedCartItemId) {

        log.error("Fallback executed for selectedCartItemId: {}, reason: {}",
                selectedCartItemId);

        // if you want to stop cart operation
        throw new RuntimeException(
                "Cart service is temporarily unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    private boolean isInStock(Long productId, int quantity) {

        boolean flag = inventoryClient.isInStock(
                productId, quantity);

        return flag;
    }

    private boolean inventoryFallback(Long productId, int quantity, Exception ex) {
        log.error("Fallback executed for selectedCartItemId: {}, reason: {}",
                productId);

        // if you want to stop cart operation
        throw new RuntimeException(
                "Inventory service is temporarily unavailable. Please try again later.");
    }

}
