package com.upeu.pagos.config;

import java.io.File;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.kafka.support.serializer.JsonSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaYamlConfigurationTest {

    @Test
    void pagoMsConfiguresEventoPagoProducerAsJsonInDevAndProd() {
        assertProducerSerializers("infra/config/config-repo/pago-ms-dev.yml");
        assertProducerSerializers("infra/config/config-repo/pago-ms-prod.yml");
    }

    private void assertProducerSerializers(String path) {
        Properties properties = loadYaml(path);

        assertThat(properties.getProperty("spring.kafka.producer.key-serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.StringSerializer");
        assertThat(properties.getProperty("spring.kafka.producer.value-serializer"))
                .isEqualTo(JsonSerializer.class.getName());
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
