package com.demo.config;

import com.demo.event.ProductCreatedEvent;
import com.demo.event.StockUpdateEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

//    @Bean
//    public ConsumerFactory<String, ProductCreatedEvent> consumerFactory() {
//        Map<String, Object> config = new HashMap<>();
//        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        config.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service-group");
//        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        return new DefaultKafkaConsumerFactory<>(config,
//            new StringDeserializer(),
//            new JsonDeserializer<>(ProductCreatedEvent.class));
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> factory =
//            new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        return factory;
//    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> targetType, String groupId) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new JsonDeserializer<>(targetType));
    }

    // Factory for ProductCreatedEvent
    @Bean
    public ConsumerFactory<String, ProductCreatedEvent> productCreatedConsumerFactory() {
        return createConsumerFactory(ProductCreatedEvent.class, "inventory-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> productCreatedListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productCreatedConsumerFactory());
        return factory;
    }

    // Factory for ReduceStock event
    @Bean
    public ConsumerFactory<String, StockUpdateEvent> reduceStockConsumerFactory() {
        return createConsumerFactory(StockUpdateEvent.class, "inventory-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockUpdateEvent> reduceStockListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StockUpdateEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reduceStockConsumerFactory());
        return factory;
    }

    // Factory for RestoreStock event
    @Bean
    public ConsumerFactory<String, StockUpdateEvent> restoreStockConsumerFactory() {
        return createConsumerFactory(StockUpdateEvent.class, "inventory-service-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockUpdateEvent> restoreStockListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StockUpdateEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(restoreStockConsumerFactory());
        return factory;
    }
}