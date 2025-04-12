package com.readrealm.inventory.integration;

import com.readrealm.inventory.config.SchemaRegistryTestConfig;
import com.readrealm.inventory.dto.InventoryDTO;
import com.readrealm.inventory.service.InventoryService;
import com.readrealm.order.event.OrderDetails;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.order.event.PaymentStatus;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.readrealm.auth.util.MockAuthorizationUtil.mockAdminAuthorization;
import static com.readrealm.auth.util.MockAuthorizationUtil.mockCustomerAuthorization;
import static com.readrealm.order.event.PaymentStatus.CANCELED;
import static com.readrealm.order.event.PaymentStatus.REFUNDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Inventory Service Integration Test")
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = { "orders" })
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
        void when_reserving_stock_with_sufficient_quantity_should_succeed() {
                List<InventoryDTO> requests = List.of(
                                new InventoryDTO("9780062073488", 5));

                List<InventoryDTO> results = inventoryService.reserveStock(requests);

                assertThat(results).hasSize(1);
                assertThat(results.getFirst().quantity()).isEqualTo(50);
        }

        @Test
        void when_reserving_stock_with_insufficient_quantity_should_throw_bad_request() {
                List<InventoryDTO> requests = List.of(
                                new InventoryDTO("9780062073488", 1500));

                assertThatThrownBy(() -> inventoryService.reserveStock(requests))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        }

        @Test
        void when_order_is_cancelled_should_restock_inventory() throws Exception {
                TestRestockWithOrderEvent(CANCELED);
        }

        @Test
        void when_order_is_refunded_should_restock_inventory() throws Exception {
                TestRestockWithOrderEvent(REFUNDED);
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

        private void TestRestockWithOrderEvent(PaymentStatus paymentStatus) throws Exception {
                schemaRegistryClient.register("OrderEvent", new AvroSchema(OrderEvent.getClassSchema()));
                // First reserve some stock
                inventoryService.reserveStock(List.of(new InventoryDTO("9780062073488", 5)));

                // Create and send cancel order event
                OrderEvent orderEvent = new OrderEvent();
                orderEvent.setPaymentStatus(paymentStatus);
                orderEvent.setUserFirstName("test");
                orderEvent.setUserLastName("user");
                orderEvent.setUserEmail("test@test.com");
                orderEvent.setOrderId("test-order-id");
                orderEvent.setCreatedDate(Instant.now());
                orderEvent.setUpdatedDate(Instant.now());
                orderEvent.setTotalAmount(BigDecimal.TEN);
                OrderDetails orderDetails = new OrderDetails();
                orderDetails.setIsbn("9780062073488");
                orderDetails.setQuantity(5);
                orderDetails.setUnitPrice(BigDecimal.TEN);
                orderEvent.setDetails(List.of(orderDetails));

                kafkaTemplate.send("orders", orderEvent);

                // Wait for the message to be processed and verify inventory is restored
                await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                        InventoryDTO inventory = inventoryService.getInventoryByIsbn("9780062073488");
                        assertThat(inventory.quantity()).isEqualTo(55);
                });
        }
}