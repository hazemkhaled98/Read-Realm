package com.readrealm.order.integration;

import com.readrealm.order.config.RestClientTestConfig;
import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.event.InventoryStatus;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.order.model.Order;
import com.readrealm.order.model.OrderItem;
import com.readrealm.order.model.PaymentStatus;
import com.readrealm.order.repository.OrderRepository;
import com.readrealm.order.service.OrderService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.EnableWireMock;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerAuthorization;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Order Service Integration Test")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"orders", "inventory", "order-refund", "order-cancellation", "payments-failure"})
@EnableWireMock
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@Import(RestClientTestConfig.class)
class OrderServiceIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.0"));

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${spring.embedded.kafka.brokers}")
    private String kafkaBrokers;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;


    private static final String ORDERS_TOPIC = "orders";
    private static final String INVENTORY_TOPIC = "inventory";
    private static final String ORDER_CANCELLATION_TOPIC = "order-cancellation";
    private static final String ORDER_REFUND_TOPIC = "order-refund";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "orders");
    }

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
    }

    @Test
    void When_creating_order_with_valid_request_should_send_event_to_inventory_topic() {

        mockCustomerAuthorization();

        // Given
        OrderRequest orderRequest = createOrderRequest("9780553103540");

        // When

        try (Consumer<String, OrderEvent> inventoryConsumer = createTestConsumer(Collections.singletonList(INVENTORY_TOPIC))) {
            String result = orderService.createOrder(orderRequest);

            // Then
            assertThat(result).contains("Order created with ID");

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                // Verify order is saved in the database
                List<Order> orders = orderRepository.findAll();
                assertThat(orders).hasSize(1);
                Order savedOrder = orders.getFirst();
                assertThat(savedOrder.getOrderId()).isNotNull();
                assertThat(savedOrder.getUserId()).isEqualTo("testUserId");
                assertThat(savedOrder.getOrderItems()).hasSize(1);
                assertThat(savedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);
                assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.TEN);
            });

            ConsumerRecords<String, OrderEvent> consumerRecords = inventoryConsumer.poll(Duration.ofSeconds(5));
            assertThat(consumerRecords.count()).isEqualTo(1);

            OrderEvent orderEvent = consumerRecords.iterator().next().value();
            assertNotNull(orderEvent, "Order event should not be null");
            assertEquals(com.readrealm.order.event.PaymentStatus.PROCESSING, orderEvent.getPaymentStatus());
            assertEquals("testFirstName", orderEvent.getUserFirstName());
            assertEquals("testLastName", orderEvent.getUserLastName());
            assertEquals("testEmail", orderEvent.getUserEmail());
            assertThat(orderEvent.getOrderId()).isNotNull();
            assertThat(orderEvent.getOrderItems()).hasSize(1);
            assertThat(orderEvent.getTotalAmount()).isEqualByComparingTo(BigDecimal.TEN);
        }
    }

    @Test
    void When_receiving_order_for_not_existing_isbn_should_throw_exception_and_not_create_order(){
        mockCustomerAuthorization();

        // Given
        OrderRequest orderRequest = createOrderRequest("non-existing-isbn");

        // When

        try (Consumer<String, OrderEvent> inventoryConsumer = createTestConsumer(Collections.singletonList(INVENTORY_TOPIC))) {
            // Then
            assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                    .isInstanceOf(HttpClientErrorException.NotFound.class);

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                // Verify order is saved in the database
                List<Order> orders = orderRepository.findAll();
                assertThat(orders).isEmpty();
            });

            ConsumerRecords<String, OrderEvent> consumerRecords = inventoryConsumer.poll(Duration.ofSeconds(5));
            assertThat(consumerRecords.count()).isZero();
        }
    }

    @Test
    void When_getting_order_with_valid_id_should_return_order_details() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("testUserId");
        String orderId = order.getOrderId();

        // When
        OrderResponse response = orderService.getOrderById(orderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.userId()).isEqualTo("testUserId");
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void When_getting_order_for_different_user_should_throw_authorization_exception() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("differentUserId");
        String orderId = order.getOrderId();

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void when_no_authentication_provided_when_requesting_orders_should_throw_exception() {

        Order order = createAndSaveOrder("differentUserId");
        String orderId = order.getOrderId();

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void When_getting_order_with_non_existent_id_should_throw_exception() {

        mockCustomerAuthorization();

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById("non-existent-id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void When_getting_orders_by_user_id_should_return_user_orders() {

        mockCustomerAuthorization();

        // Given
        createAndSaveOrder("testUserId");
        createAndSaveOrder("testUserId");

        // When
        List<OrderResponse> responses = orderService.getOrdersByUserId("testUserId");

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).userId()).isEqualTo("testUserId");
        assertThat(responses.get(1).userId()).isEqualTo("testUserId");
    }

    @Test
    void When_getting_orders_by_user_id_should_throw_authorization_exception_for_different_logged_user() {

        mockCustomerAuthorization();

        // Given
        createAndSaveOrder("differentUserId");

        // When & Then
        assertThatThrownBy(() -> orderService.getOrdersByUserId("differentUserId"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void When_requesting_orders_by_user_id_with_no_authentication_should_throw_exception() {

        // When & Then
        assertThatThrownBy(() -> orderService.getOrdersByUserId("differentUserId"))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void When_receiving_order_event_should_update_order_status() throws Exception {
        // Given
        Order order = createAndSaveOrder("testUserId");
        String orderId = order.getOrderId();

        OrderEvent orderEvent = createOrderEvent(orderId, com.readrealm.order.event.PaymentStatus.COMPLETED);

        // When
        kafkaTemplate.send(ORDERS_TOPIC, orderEvent).get();

        // Then
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findByOrderId(orderId).orElseThrow();
            assertThat(updatedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        });
    }

    @Test
    void When_cancelling_order_with_valid_id_should_send_event_to_order_cancellation_topic() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("testUserId");
        String orderId = order.getOrderId();

        // When
        try (Consumer<String, OrderEvent> cancellationConsumer = createTestConsumer(Collections.singletonList(ORDER_CANCELLATION_TOPIC))) {
            String result = orderService.cancelOrder(orderId);

            // Then
            assertThat(result).contains("Order cancellation request is sent Successfully");

            ConsumerRecords<String, OrderEvent> consumerRecords = cancellationConsumer.poll(Duration.ofSeconds(5));
            assertThat(consumerRecords.count()).isEqualTo(1);

            OrderEvent orderEvent = consumerRecords.iterator().next().value();
            assertNotNull(orderEvent);
            assertEquals(orderId, orderEvent.getOrderId());
        }
    }

    @Test
    void When_cancelling_paid_order_should_throw_exception() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("testUserId");
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);
        String orderId = order.getOrderId();

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("is already paid");
    }

    @Test
    void When_cancelling_order_with_invalid_id_should_throw_exception() {

        mockCustomerAuthorization();

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder("non-existent-id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void When_cancelling_order_with_different_user_id_should_throw_exception() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("differentUserId");
        String orderId = order.getOrderId();

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void When_cancelling_order_with_no_authentication_throw_exception() {

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder("orderId"))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void When_refund_order_with_valid_id_should_send_event_to_order_refund_topic() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("testUserId");
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);
        String orderId = order.getOrderId();

        // When
        try (Consumer<String, OrderEvent> refundConsumer = createTestConsumer(Collections.singletonList(ORDER_REFUND_TOPIC))) {
            String result = orderService.refundOrder(orderId);

            // Then
            assertThat(result).contains("Order refund Request is sent successfully");

            ConsumerRecords<String, OrderEvent> consumerRecords = refundConsumer.poll(Duration.ofSeconds(5));
            assertThat(consumerRecords.count()).isEqualTo(1);

            OrderEvent orderEvent = consumerRecords.iterator().next().value();
            assertNotNull(orderEvent);
            assertEquals(orderId, orderEvent.getOrderId());
        }
    }

    @Test
    void When_refund_not_paid_order_should_throw_exception() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("testUserId");
        String orderId = order.getOrderId();

        // When & Then
        assertThatThrownBy(() -> orderService.refundOrder(orderId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not paid yet");
    }

    @Test
    void When_refund_order_with_invalid_id_should_throw_exception() {

        mockCustomerAuthorization();

        // When & Then
        assertThatThrownBy(() -> orderService.refundOrder("non-existent-id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void When_refund_order_with_different_user_id_should_throw_exception() {

        mockCustomerAuthorization();

        // Given
        Order order = createAndSaveOrder("differentUserId");
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);
        String orderId = order.getOrderId();

        // When & Then
        assertThatThrownBy(() -> orderService.refundOrder(orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void When_refund_order_with_no_authentication_should_throw_exception() {

        // When & Then
        assertThatThrownBy(() -> orderService.refundOrder("orderId"))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private Order createAndSaveOrder(String userId) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setPaymentStatus(PaymentStatus.PROCESSING);
        order.setTotalAmount(BigDecimal.TEN);

        OrderItem orderItem = new OrderItem();
        orderItem.setIsbn("9780062073488");
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(BigDecimal.TEN);
        order.setOrderItems(List.of(orderItem));

        order.setCreatedDate(Instant.now());
        order.setUpdatedDate(Instant.now());

        return orderRepository.save(order);
    }

    private OrderRequest createOrderRequest(String isbn) {
        com.readrealm.order.dto.OrderItem orderItem = new com.readrealm.order.dto.OrderItem(isbn, 1, BigDecimal.valueOf(30));
        return new OrderRequest(List.of(orderItem));
    }

    private KafkaConsumer<String, OrderEvent> createTestConsumer(List<String> topics) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        props.put("schema.registry.url", "mock://test");
        props.put("specific.avro.reader", true);
        KafkaConsumer<String, OrderEvent> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(topics);
        return consumer;
    }

    private OrderEvent createOrderEvent(String orderId, com.readrealm.order.event.PaymentStatus paymentStatus) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(orderId);
        orderEvent.setPaymentStatus(paymentStatus);
        orderEvent.setUserFirstName("testFirstName");
        orderEvent.setUserLastName("testLastName");
        orderEvent.setUserEmail("testEmail");
        orderEvent.setCreatedDate(Instant.now());
        orderEvent.setUpdatedDate(Instant.now());
        orderEvent.setTotalAmount(BigDecimal.valueOf(29.99));

        com.readrealm.order.event.OrderItem orderItem = new com.readrealm.order.event.OrderItem();
        orderItem.setIsbn("9780062073488");
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(BigDecimal.valueOf(29.99));
        orderItem.setInventoryStatus(InventoryStatus.PROCESSING);
        orderEvent.setOrderItems(List.of(orderItem));

        return orderEvent;
    }
}