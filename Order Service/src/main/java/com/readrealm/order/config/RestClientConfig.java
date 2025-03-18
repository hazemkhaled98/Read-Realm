package com.readrealm.order.config;

import com.readrealm.order.client.CatalogClient;
import com.readrealm.order.client.InventoryClient;
import com.readrealm.order.client.PaymentClient;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public InventoryClient inventoryClient(RestClient.Builder restClientBuilder) {
        return createClient(restClientBuilder, "http://localhost:8082", InventoryClient.class);
    }

    @Bean
    public PaymentClient paymentClient( RestClient.Builder restClientBuilder) {
        return createClient(restClientBuilder, "http://localhost:8083", PaymentClient.class);
    }

    @Bean
    public CatalogClient catalogClient( RestClient.Builder restClientBuilder) {
        return createClient(restClientBuilder, "http://localhost:8080", CatalogClient.class);
    }

    private <T> T createClient(RestClient.Builder restClientBuilder, String baseUrl, Class<T> clientClass) {
        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(createClientRequestFactory())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(clientClass);
    }

    private ClientHttpRequestFactory createClientRequestFactory() {
        ClientHttpRequestFactorySettings clientHttpRequestFactorySettings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(3))
                .withReadTimeout(Duration.ofSeconds(3));
        return ClientHttpRequestFactories.get(clientHttpRequestFactorySettings);
    }
}
