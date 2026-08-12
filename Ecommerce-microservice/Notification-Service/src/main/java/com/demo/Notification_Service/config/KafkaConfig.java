package com.demo.Notification_Service.config;

import com.demo.Notification_Service.event.OrderPlacedEvent;
import com.demo.Notification_Service.event.OrderStatusChangedEvent;
import com.demo.Notification_Service.event.PaymentFailedEvent;
import com.demo.Notification_Service.event.PaymentSuccessEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.*;
import java.util.*;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

//    @Bean
//    public ConsumerFactory<String, Object> consumerFactory() {
//        Map<String, Object> config = new HashMap<>();
//        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        config.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
//        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
//            StringDeserializer.class);
//        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
//            JsonDeserializer.class);
//        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        return new DefaultKafkaConsumerFactory<>(config,
//            new StringDeserializer(),
//            new JsonDeserializer<>(Object.class, false));
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, Object>
//            kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
//            new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        return factory;
//    }


    // Common properties
    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    // ==================== Consumer Factories ====================

    @Bean
    public ConsumerFactory<String, OrderPlacedEvent> orderPlacedConsumerFactory() {
        JsonDeserializer<OrderPlacedEvent> deserializer =
                new JsonDeserializer<>(OrderPlacedEvent.class, false);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConsumerFactory<String, PaymentSuccessEvent> paymentSuccessConsumerFactory() {
        JsonDeserializer<PaymentSuccessEvent> deserializer =
                new JsonDeserializer<>(PaymentSuccessEvent.class, false);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory() {
        JsonDeserializer<PaymentFailedEvent> deserializer =
                new JsonDeserializer<>(PaymentFailedEvent.class, false);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConsumerFactory<String, OrderStatusChangedEvent> orderStatusChangedConsumerFactory() {
        JsonDeserializer<OrderStatusChangedEvent> deserializer =
                new JsonDeserializer<>(OrderStatusChangedEvent.class, false);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer
        );
    }

    // ==================== Listener Container Factories ====================

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent>
    orderPlacedKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderPlacedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderPlacedConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent>
    paymentSuccessKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentSuccessConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent>
    paymentFailedKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentFailedConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangedEvent>
    orderStatusChangedKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderStatusChangedConsumerFactory());
        return factory;
    }
}