package com.readrealm.payment.config;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SchemaRegistryTestConfig {


    @Bean
    public SchemaRegistryClient schemaRegistryClient() {
        return new MockSchemaRegistryClient();
    }
}