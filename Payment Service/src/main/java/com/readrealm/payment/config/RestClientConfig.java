package com.readrealm.payment.config;

import com.readrealm.payment.client.OrderClient;
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
    public OrderClient orderClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder.baseUrl("http://ORDER-SERVICE").build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(OrderClient.class);
    }
}
