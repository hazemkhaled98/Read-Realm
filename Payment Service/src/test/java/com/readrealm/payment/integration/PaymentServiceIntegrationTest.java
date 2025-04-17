package com.readrealm.payment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.readrealm.payment.config.SchemaRegistryTestConfig;
import com.readrealm.payment.dto.PaymentRequest;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.dto.StripeWebhookRequest;
import com.readrealm.payment.dto.StripeWebhookRequest.Data;
import com.readrealm.payment.dto.StripeWebhookRequest.SetupIntent;
import com.readrealm.payment.event.ConfirmPaymentEvent;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import com.readrealm.payment.repository.PaymentRepository;
import com.readrealm.payment.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment Service Integration Test")
@Testcontainers
@EnableWireMock
@EmbeddedKafka(partitions = 1, topics = {"order-confirmation"})
@Import(SchemaRegistryTestConfig.class)
@ActiveProfiles("test")
class PaymentServiceIntegrationTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.0"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "payments-test");
        registry.add("stripe.api.key", () -> "sk_test");
        registry.add("stripe.webhook.secret", () -> "whsec_test");
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private KafkaTemplate<String, ConfirmPaymentEvent> kafkaTemplate;

    @InjectWireMock
    private WireMockServer wireMock;

    @SpyBean
    private ObjectMapper objectMapper;

    @Autowired
    private SchemaRegistryClient schemaRegistryClient;

    private StripeStubs stripeStubs;

    @BeforeAll
    static void setupContainer() {
        mongoDBContainer.start();
    }

    @BeforeEach
    void setup() {
        Stripe.overrideApiBase(wireMock.baseUrl());
        paymentRepository.deleteAll();
        stripeStubs = new StripeStubs(wireMock);
    }

    @AfterAll
    static void closeContainer() {
        mongoDBContainer.close();
    }

    @Test
    void When_creating_payment_with_valid_request_should_succeed() {
        // Given
        String orderId = generateUniqueOrderId();
        stripeStubs.stubPaymentIntentCreation();

        // When
        PaymentRequest request = new PaymentRequest(orderId, BigDecimal.TEN, "USD");
        PaymentResponse response = paymentService.createPayment(request);

        // Then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.stripePaymentIntentId()).isEqualTo("pi_test");
        assertThat(response.clientSecret()).isEqualTo("test_secret");
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.amount()).isEqualTo(BigDecimal.TEN);
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.createdDate()).isNotNull();
        assertThat(response.updatedDate()).isNotNull();

        Payment savedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
        assertThat(savedPayment.getStripePaymentIntentId()).isEqualTo("pi_test");
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void When_creating_payment_with_duplicate_order_id_should_throw_exception() {
        // Given
        String orderId = generateUniqueOrderId();
        stripeStubs.stubPaymentIntentCreation();

        paymentService.createPayment(new PaymentRequest(orderId, BigDecimal.TEN, "USD"));

        // When & Then
        PaymentRequest duplicateRequest = new PaymentRequest(orderId, BigDecimal.TEN, "USD");
        assertThatThrownBy(() -> paymentService.createPayment(duplicateRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void When_stripe_api_returns_error_should_throw_exception() {
        // Given
        String orderId = generateUniqueOrderId();
        stripeStubs.stubPaymentIntentCreationError();

        // When & Then
        PaymentRequest request = new PaymentRequest(orderId, BigDecimal.TEN, "USD");
        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void When_getting_payment_with_valid_order_id_should_return_payment() {
        // Given
        String orderId = generateUniqueOrderId();
        createTestPayment(orderId);

        // When
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        // Then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.stripePaymentIntentId()).isEqualTo("pi_test");
    }

    @Test
    void When_getting_payment_with_non_existent_order_id_should_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> paymentService.getPaymentByOrderId("non-existent-id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void When_cancelling_payment_with_valid_order_id_should_succeed() {
        // Given
        String orderId = generateUniqueOrderId();
        createTestPayment(orderId);

        stripeStubs.stubPaymentIntentRetrieval("required_payment_method");
        stripeStubs.stubPaymentIntentCancellation();

        // When
        PaymentResponse response = paymentService.cancelPayment(orderId);

        // Then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(PaymentStatus.CANCELED);

        // Verify payment status was updated in database
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    void When_refunding_payment_with_valid_order_id_should_succeed() {
        // Given
        String orderId = generateUniqueOrderId();
        createTestPayment(orderId);

        stripeStubs.stubPaymentIntentRetrieval("completed");
        stripeStubs.stubRefundCreation();

        // When
        PaymentResponse response = paymentService.refundPayment(orderId);

        // Then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);

        // Verify payment status was updated in database
        Payment updatedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void When_handling_valid_stripe_webhook_should_update_payment_status_and_send_kafka_message() throws IOException, RestClientException {
        // Given
        String orderId = generateUniqueOrderId();
        createTestPayment(orderId);
        Event paymentSuccessEvent = mock(Event.class);
        StripeWebhookRequest stripeWebhookRequest = mock(StripeWebhookRequest.class);
        Data data = mock(Data.class);
        SetupIntent setupIntent = mock(SetupIntent.class);
        Map<String, String> metadata = mock(Map.class);
        schemaRegistryClient.register("ConfirmPaymentEvent", new AvroSchema(ConfirmPaymentEvent.getClassSchema()));

        String payload = stripeStubs.getValidStripeWebhookPayload(orderId);

        try (MockedStatic<Webhook> webhookMockedStatic = Mockito.mockStatic(Webhook.class)) {
            webhookMockedStatic.when(() ->
                    Webhook.constructEvent(
                            anyString(),
                            anyString(),
                            anyString()
                    )
            ).thenReturn(paymentSuccessEvent);

            when(paymentSuccessEvent.getType()).thenReturn("payment_intent.succeeded");
            when(objectMapper.readValue(payload, StripeWebhookRequest.class)).thenReturn(stripeWebhookRequest);
            when(stripeWebhookRequest.data()).thenReturn(data);
            when(data.setupIntent()).thenReturn(setupIntent);
            when(setupIntent.metadata()).thenReturn(metadata);
            when(metadata.get("orderId")).thenReturn(orderId);

            // When
            paymentService.handleStripeWebhook("test", payload);
        }

        // Then
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        // Verify Kafka message was sent
        verify(kafkaTemplate).send(eq("order-confirmation"), any(ConfirmPaymentEvent.class));
    }


    @Test
    void When_handling_stripe_webhook_with_invalid_signature_should_throw_exception() {
        // Given
        String webhookPayload = """
                {
                    "data": {
                        "setup_intent": {
                            "metadata": {
                                "orderId": "test-order-id"
                            }
                        }
                    }
                }
                """;
        String invalidSignature = "invalid_signature";

        // When & Then
        assertThatThrownBy(() -> paymentService.handleStripeWebhook(invalidSignature, webhookPayload))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Invalid Stripe signature");
    }

    private String generateUniqueOrderId() {
        return "order-" + UUID.randomUUID();
    }

    private void createTestPayment(String orderId) {
        stripeStubs.stubPaymentIntentCreation();
        paymentService.createPayment(new PaymentRequest(orderId, BigDecimal.TEN, "USD"));
    }
}