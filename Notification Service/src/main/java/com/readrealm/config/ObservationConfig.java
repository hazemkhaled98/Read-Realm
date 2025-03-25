package com.readrealm.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
public class ObservationConfig {

    private final ConcurrentKafkaListenerContainerFactory containerFactory;
    private final ProducerFactory producerFactory;
    private final ConsumerFactory consumerFactory;
    private final MeterRegistry meterRegistry;
    private final KafkaTemplate kafkaTemplate;

    @Bean
    ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }


    @PostConstruct
    public void setObservationForKafkaTemplate(){
        containerFactory.getContainerProperties().setObservationEnabled(true);
        kafkaTemplate.setObservationEnabled(true);
        producerFactory.addListener(new MicrometerProducerListener(meterRegistry));
        consumerFactory.addListener(new MicrometerConsumerListener(meterRegistry));
    }
}
