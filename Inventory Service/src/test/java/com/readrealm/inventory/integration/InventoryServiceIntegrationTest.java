package com.readrealm.inventory.integration;

import com.readrealm.inventory.config.SchemaRegistryTestConfig;
import com.readrealm.inventory.dto.InventoryDTO;
import com.readrealm.inventory.service.InventoryService;
import com.readrealm.order.event.InventoryStatus;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.order.event.OrderItem;
import com.readrealm.order.event.PaymentStatus;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockAdminAuthorization;
import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerAuthorization;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Inventory Service Integration Test")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"orders", "payments"})
@DirtiesContext
@ActiveProfiles("test")
@Import(SchemaRegistryTestConfig.class)
@Sql(scripts = "/setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InventoryServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.3.0")
            .withDatabaseName("inventory-db-test")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Autowired
    private SchemaRegistryClient schemaRegistryClient;

    @Value("${spring.embedded.kafka.brokers}")
    private String kafkaBrokers;

    @Test
    void when_admin_creates_inventory_should_save_successfully() {
        mockAdminAuthorization();
        InventoryDTO request = new InventoryDTO("9780062073489", 15);

        InventoryDTO created = inventoryService.createInventory(request);

        assertThat(created.isbn()).isEqualTo(request.isbn());
        assertThat(created.quantity()).isEqualTo(request.quantity());
    }

    @Test
    void when_customer_tries_to_create_inventory_should_throw_access_denied() {
        mockCustomerAuthorization();
        InventoryDTO request = new InventoryDTO("9780062073489", 15);

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void when_creating_duplicate_isbn_should_throw_bad_request() {
        mockAdminAuthorization();
        InventoryDTO request = new InventoryDTO("9780062073488", 15);

        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void when_updating_inventory_should_update_quantity() {
        InventoryDTO request = new InventoryDTO("9780062073488", 5);

        InventoryDTO updated = inventoryService.updateInventory(request);

        assertThat(updated.quantity()).isEqualTo(60);
    }

    @Test
    void when_reserving_stock_with_sufficient_quantity_should_succeed() throws RestClientException, IOException {
        try(KafkaConsumer<String, OrderEvent> consumer = createTestConsumer("payments");){
            schemaRegistryClient.register("OrderEvent", new AvroSchema(OrderEvent.getClassSchema()));
            OrderEvent orderEvent = createOrderEvent(5);
            kafkaTemplate.send("inventory", orderEvent);
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                InventoryDTO inventory = inventoryService.getInventoryByIsbn("9780062073488");
                assertThat(inventory.quantity()).isEqualTo(50);
            });
            ConsumerRecords<String, OrderEvent> consumerRecords = consumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();

            assertThat(receivedOrderEvent.getOrderItems().getFirst().getInventoryStatus())
                    .isEqualTo(InventoryStatus.IN_STOCK);
        }
    }

    @Test
    void when_reserving_stock_with_insufficient_quantity_should_throw_exception() throws RestClientException, IOException {
        try(KafkaConsumer<String, OrderEvent> consumer = createTestConsumer("orders");){
            schemaRegistryClient.register("OrderEvent", new AvroSchema(OrderEvent.getClassSchema()));
            OrderEvent orderEvent = createOrderEvent(100);
            kafkaTemplate.send("inventory", orderEvent);
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                InventoryDTO inventory = inventoryService.getInventoryByIsbn("9780062073488");
                assertThat(inventory.quantity()).isEqualTo(55);
            });
            assertThatThrownBy(() -> inventoryService.reserveStock(orderEvent))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);

            ConsumerRecords<String, OrderEvent> consumerRecords = consumer.poll(Duration.ofSeconds(5));
            OrderEvent receivedOrderEvent = consumerRecords.iterator().next().value();

            assertThat(receivedOrderEvent.getOrderItems().getFirst().getInventoryStatus())
                    .isEqualTo(InventoryStatus.OUT_OF_STOCK);
        }
    }

    @Test
    void when_order_is_cancelled_should_restock_inventory() throws Exception {
        TestRestockWithOrderEvent("order-cancellation");
    }

    @Test
    void when_order_is_refunded_should_restock_inventory() throws Exception {
        TestRestockWithOrderEvent("order-refund");
    }

    @Test
    void when_admin_deletes_inventory_should_succeed() {
        mockAdminAuthorization();
        assertThatNoException().isThrownBy(() -> inventoryService.deleteInventory("9780062073488"));

        assertThatThrownBy(() -> inventoryService.getInventoryByIsbn("9780062073488"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void when_customer_tries_to_delete_inventory_should_throw_access_denied() {
        mockCustomerAuthorization();
        assertThatThrownBy(() -> inventoryService.deleteInventory("9780062073488"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private void TestRestockWithOrderEvent(String topic) throws Exception {
        schemaRegistryClient.register("OrderEvent", new AvroSchema(OrderEvent.getClassSchema()));
        // First reserve some stock
        OrderEvent orderEvent = createOrderEvent(5);
        kafkaTemplate.send("inventory", orderEvent);


        // Create and send cancel order event
        kafkaTemplate.send(topic, orderEvent);

        // Wait for the message to be processed and verify inventory is restored
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            InventoryDTO inventory = inventoryService.getInventoryByIsbn("9780062073488");
            assertThat(inventory.quantity()).isEqualTo(55);
        });
    }

    private static @NotNull OrderEvent createOrderEvent(int quantity) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setPaymentStatus(PaymentStatus.PROCESSING);
        orderEvent.setUserFirstName("test");
        orderEvent.setUserLastName("user");
        orderEvent.setUserEmail("test@test.com");
        orderEvent.setOrderId("test-order-id");
        orderEvent.setCreatedDate(Instant.now());
        orderEvent.setUpdatedDate(Instant.now());
        orderEvent.setTotalAmount(BigDecimal.TEN);
        OrderItem orderItem = new OrderItem();
        orderItem.setIsbn("9780062073488");
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(BigDecimal.TEN);
        orderItem.setInventoryStatus(InventoryStatus.PROCESSING);
        orderEvent.setOrderItems(List.of(orderItem));
        return orderEvent;
    }

    private KafkaConsumer<String, OrderEvent> createTestConsumer(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", "mock://test");
        props.put("specific.avro.reader", true);
        KafkaConsumer<String, OrderEvent> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;

    }
}