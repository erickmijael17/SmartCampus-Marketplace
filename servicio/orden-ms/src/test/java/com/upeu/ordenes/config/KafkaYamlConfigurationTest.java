package com.upeu.ordenes.config;

import java.io.File;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaYamlConfigurationTest {

    @Test
    void ordenMsConfiguresPagoAprobadoConsumerAsJsonInDevAndProd() {
        assertConsumerDeserializers("infra/config/config-repo/orden-ms-dev.yml");
        assertConsumerDeserializers("infra/config/config-repo/orden-ms-prod.yml");
    }

    private void assertConsumerDeserializers(String path) {
        Properties properties = loadYaml(path);

        assertThat(properties.getProperty("spring.kafka.consumer.key-deserializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringDeserializer");
        assertThat(properties.getProperty("spring.kafka.consumer.value-deserializer"))
                .isEqualTo(JsonDeserializer.class.getName());
        assertThat(properties.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"))
                .isEqualTo("*");
        assertThat(properties.getProperty("spring.kafka.consumer.properties.spring.json.value.default.type"))
                .isEqualTo("com.upeu.ordenes.evento.EventoPagoAprobado");
        assertThat(properties.getProperty("spring.kafka.consumer.properties.spring.json.use.type.headers"))
                .isEqualTo("false");
    }

    private Properties loadYaml(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file = new File("../../" + path);
        }
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new FileSystemResource(file));
        Properties properties = factory.getObject();
        return properties != null ? properties : new Properties();
    }
}
