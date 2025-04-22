package com.readrealm.payment.service;

import com.readrealm.order.event.InventoryStatus;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.order.event.OrderItem;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import com.readrealm.payment.paymentgateway.PaymentGateway;
import com.readrealm.payment.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.readrealm.order.event.PaymentStatus.CANCELED;
import static com.readrealm.order.event.PaymentStatus.COMPLETED;
import static com.readrealm.order.event.PaymentStatus.PENDING;
import static com.readrealm.order.event.PaymentStatus.PROCESSING;
import static com.readrealm.order.event.PaymentStatus.REFUNDED;
import static com.readrealm.order.event.PaymentStatus.REQUIRES_PAYMENT_METHOD;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment Service Integration Test")
@Testcontainers
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"orders", "order-refund", "order-cancellation", "payments"})
@ActiveProfiles("test")
class PaymentServiceIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.0"));

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @SpyBean
    private PaymentGateway mockPaymentGateway;


    @Value("${spring.embedded.kafka.brokers}")
    private String kafkaBrokers;

    private static final String PAYMENTS_TOPIC = "payments";

    @BeforeEach
    void setup(){
        paymentRepository.deleteAll();
        reset(mockPaymentGateway);
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "payments");
    }

    @Test
    void should_create_payment_and_persist_to_database_when_order_event_received() throws Exception {

        try (KafkaConsumer<String, OrderEvent> ordersConsumer = createOrdersTopicConsumer()) {

            OrderEvent orderEvent = createOrderEvent();
            String orderId = orderEvent.getOrderId();
            kafkaTemplate.send(PAYMENTS_TOPIC, orderEvent).get();
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(orderId);
                assertNotNull(payment, "Payment should be saved in database");
                assertEquals(orderId, payment.getOrderId());
                assertEquals(PaymentStatus.PENDING, payment.getStatus());
                assertEquals("mockPaymentId", payment.getPaymentRequestId());
                assertEquals("mockClientSecret", payment.getClientSecret());
            });
            ConsumerRecords<String, OrderEvent> consumerRecords = ordersConsumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();

            assertNotNull(receivedOrderEvent, "Updated order event should not be null");
            assertEquals(PENDING, receivedOrderEvent.getPaymentStatus());
        }
    }

    @Test
    void should_not_create_duplicate_payment_with_pending_status() throws Exception {
        // Given
        OrderEvent orderEvent = createOrderEvent();
        String orderId = orderEvent.getOrderId();

        // Create an existing payment
        createPayment(orderId);

        // When
        kafkaTemplate.send(PAYMENTS_TOPIC, orderEvent).get();

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Payment> payments = paymentRepository.findAll();
            assertEquals(1, payments.size(), "Should have only one payment");
            assertEquals("existingPaymentId", payments.getFirst().getPaymentRequestId(), "Payment ID should not change");
        });

        // Verify payment gateway was not called
        verify(mockPaymentGateway, never()).createPaymentRequest(anyString(), any(BigDecimal.class));
    }

    @Test
    void should_handle_payment_gateway_exception_and_dont_create_payments() throws Exception {

        // Given
        OrderEvent orderEvent = createOrderEvent();
        String orderId = orderEvent.getOrderId();

        // Mock payment gateway to throw exception
        when(mockPaymentGateway.createPaymentRequest(eq(orderId), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Payment gateway error"));

        try (Consumer<String, OrderEvent> ordersConsumer = createOrdersTopicConsumer()) {
            // When
            kafkaTemplate.send(PAYMENTS_TOPIC, orderEvent).get();
            // Verify no payment was created
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Optional<Payment> payment = Optional.ofNullable(paymentRepository.findByOrderId(orderId));
                assertFalse(payment.isPresent(), "No payment should be saved when gateway fails");
            });

            ConsumerRecords<String, OrderEvent> consumerRecords = ordersConsumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();


            assertNotNull(receivedOrderEvent, "Updated order event should not be null");
            assertEquals(REQUIRES_PAYMENT_METHOD, receivedOrderEvent.getPaymentStatus());
        }

    }

    @Test
    void should_cancel_payment_request_when_receiving_event_in_cancellation_topic() throws Exception {
        // Given
        OrderEvent orderEvent = createOrderEvent();
        String orderId = orderEvent.getOrderId();

        // Create an existing payment
        createPayment(orderId);

        try (Consumer<String, OrderEvent> ordersConsumer = createOrdersTopicConsumer()) {

            // When
            kafkaTemplate.send("order-cancellation", orderEvent).get();

            // Verify payment was updated
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(orderId);
                assertNotNull(payment, "Payment should exist");
                assertEquals(PaymentStatus.CANCELED, payment.getStatus());
            });

            ConsumerRecords<String, OrderEvent> consumerRecords = ordersConsumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();

            assertNotNull(receivedOrderEvent, "Updated order event should not be null");
            assertEquals(CANCELED, receivedOrderEvent.getPaymentStatus());

            // Verify payment gateway was called correctly
            verify(mockPaymentGateway).cancelPayment("existingPaymentId");
        }

    }

    @Test
    void should_refund_payment_when_receiving_event_in_refund_topic() throws Exception {
        // Given
        OrderEvent orderEvent = createOrderEvent();
        String orderId = orderEvent.getOrderId();

        // Create an existing payment
        createPayment(orderId);

        try (Consumer<String, OrderEvent> ordersConsumer = createOrdersTopicConsumer()) {

            // When
            kafkaTemplate.send("order-refund", orderEvent).get();

            // Verify payment was updated
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(orderId);
                assertNotNull(payment, "Payment should exist");
                assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
            });

            ConsumerRecords<String, OrderEvent> consumerRecords = ordersConsumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();

            assertNotNull(receivedOrderEvent, "Updated order event should not be null");
            assertEquals(REFUNDED, receivedOrderEvent.getPaymentStatus());

            // Verify payment gateway was called correctly
            verify(mockPaymentGateway).refundPayment("existingPaymentId");
        }
    }

    @Test
    void should_handle_webhook_event_for_completed_payment() {

        String orderId = UUID.randomUUID().toString();

        // Create an existing payment
        createPayment(orderId);

        // When
        paymentService.processWebhookEvent(orderId, PaymentStatus.COMPLETED);

        try (Consumer<String, OrderEvent> ordersConsumer = createOrdersTopicConsumer()) {

            // Verify payment was updated
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(orderId);
                assertNotNull(payment, "Payment should exist");
                assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
            });

            ConsumerRecords<String, OrderEvent> consumerRecords = ordersConsumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();

            assertNotNull(receivedOrderEvent, "Updated order event should not be null");
            assertEquals(COMPLETED, receivedOrderEvent.getPaymentStatus());

        }

    }

    @Test
    void should_handle_webhook_event_for_failed_payment() {
        String orderId = UUID.randomUUID().toString();

        // Create an existing payment
        createPayment(orderId);

        // When
        paymentService.processWebhookEvent(orderId, PaymentStatus.REQUIRES_PAYMENT_METHOD);

        try (Consumer<String, OrderEvent> ordersConsumer = createOrdersTopicConsumer()) {

            // Verify payment was updated
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(orderId);
                assertNotNull(payment, "Payment should exist");
                assertEquals(PaymentStatus.REQUIRES_PAYMENT_METHOD, payment.getStatus());
            });

            ConsumerRecords<String, OrderEvent> consumerRecords = ordersConsumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();

            assertNotNull(receivedOrderEvent, "Updated order event should not be null");
            assertEquals(REQUIRES_PAYMENT_METHOD, receivedOrderEvent.getPaymentStatus());

        }
    }

    private static @NotNull OrderEvent createOrderEvent() {
        String orderId = UUID.randomUUID().toString();
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setPaymentStatus(PROCESSING);
        orderEvent.setUserFirstName("test");
        orderEvent.setUserLastName("user");
        orderEvent.setUserEmail("test@test.com");
        orderEvent.setOrderId(orderId);
        orderEvent.setCreatedDate(Instant.now());
        orderEvent.setUpdatedDate(Instant.now());
        orderEvent.setTotalAmount(BigDecimal.TEN);
        OrderItem orderItem = new OrderItem();
        orderItem.setIsbn("9780062073488");
        orderItem.setQuantity(5);
        orderItem.setUnitPrice(BigDecimal.TEN);
        orderItem.setInventoryStatus(InventoryStatus.PROCESSING);
        orderEvent.setOrderItems(List.of(orderItem));
        return orderEvent;
    }

    private KafkaConsumer<String, OrderEvent> createOrdersTopicConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        props.put("schema.registry.url", "mock://test");
        props.put("specific.avro.reader", true);
        KafkaConsumer<String, OrderEvent> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("orders"));
        return consumer;
    }

    private void createPayment(String orderId) {
        Payment existingPayment = new Payment();
        existingPayment.setOrderId(orderId);
        existingPayment.setAmount(BigDecimal.TEN);
        existingPayment.setStatus(PaymentStatus.PENDING);
        existingPayment.setPaymentRequestId("existingPaymentId");
        existingPayment.setClientSecret("existingClientSecret");
        existingPayment.setCreatedDate(Instant.now());
        existingPayment.setUpdatedDate(Instant.now());
        existingPayment.setOrderEvent(createOrderEvent());
        paymentRepository.save(existingPayment);
    }
}