package com.upeu.chat.config;

import com.upeu.chat.dto.EventoPago;
import com.upeu.chat.dto.EventoVentaConfirmada;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
public class KafkaConfiguracion {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfiguracion.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:41092}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, EventoPago> consumerFactory() {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        propiedades.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-ms-group");
        propiedades.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propiedades.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        propiedades.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        propiedades.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        propiedades.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        propiedades.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        propiedades.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EventoPago.class.getName());
        propiedades.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(
                propiedades,
                new ErrorHandlingDeserializer<>(new StringDeserializer()),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>())
        );
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) ->
                log.error(
                        "Mensaje descartado de Kafka en topic={}, particion={}, offset={}: {}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        exception.getMessage(),
                        exception
                ),
                new FixedBackOff(0L, 0L)
        );
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventoPago> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventoPago> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, EventoVentaConfirmada> ventaConfirmadaConsumerFactory() {
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        propiedades.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-ms-group");
        propiedades.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propiedades.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        propiedades.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        propiedades.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        propiedades.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        propiedades.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        propiedades.put(JsonDeserializer.VALUE_DEFAULT_TYPE, EventoVentaConfirmada.class.getName());
        propiedades.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(
                propiedades,
                new ErrorHandlingDeserializer<>(new StringDeserializer()),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>())
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventoVentaConfirmada> ventaConfirmadaKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventoVentaConfirmada> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ventaConfirmadaConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler());
        return factory;
    }
}
