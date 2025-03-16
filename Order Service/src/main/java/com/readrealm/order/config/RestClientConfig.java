package com.readrealm.order.config;

import com.readrealm.order.client.CatalogClient;
import com.readrealm.order.client.InventoryClient;
import com.readrealm.order.client.PaymentClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }


    @Bean
    public InventoryClient inventoryClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        return createClient(restClientBuilder, "http://INVENTORY-SERVICE", InventoryClient.class);
    }

    @Bean
    public PaymentClient paymentClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        return createClient(restClientBuilder, "http://PAYMENT-SERVICE", PaymentClient.class);
    }

    @Bean
    public CatalogClient catalogClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        return createClient(restClientBuilder, "http://CATALOG-SERVICE", CatalogClient.class);
    }

    private <T> T createClient(RestClient.Builder restClientBuilder, String baseUrl, Class<T> clientClass) {
        RestClient restClient = restClientBuilder.baseUrl(baseUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(clientClass);
    }
}
