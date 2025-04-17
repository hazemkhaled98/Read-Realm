package com.readrealm.payment.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.readrealm.payment.dto.PaymentRequest;
import com.readrealm.payment.dto.PaymentResponse;
import com.readrealm.payment.model.Payment;
import com.readrealm.payment.model.PaymentStatus;
import com.readrealm.payment.repository.PaymentRepository;
import com.readrealm.payment.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment Service Integration Test")
@Testcontainers
@EnableWireMock
@EmbeddedKafka(partitions = 1, topics = {"order-confirmation"})
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
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectWireMock
    private WireMockServer wireMock;

    @BeforeAll
    static void setupContainer() {
        mongoDBContainer.start();
    }

    @BeforeEach
    void setup() {
        Stripe.overrideApiBase(wireMock.baseUrl());

        paymentRepository.deleteAll();

    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @AfterAll
    static void closeContainer() {
        mongoDBContainer.close();
    }

    @Test
    void When_creating_payment_with_valid_request_should_succeed() {

        // Given
        String orderId = generateUniqueOrderId();
        createTestPayment(orderId);

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
        createTestPayment(orderId);

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
        wireMock.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "error": {
                                        "message": "Invalid request"
                                    }
                                }
                                """)));

        // When & Then
        PaymentRequest request = new PaymentRequest(orderId, BigDecimal.TEN, "USD");
        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void When_getting_payment_with_valid_order_id_should_return_payment() {

        String orderId = generateUniqueOrderId();

        createTestPayment(orderId);

        paymentService.createPayment(new PaymentRequest(orderId, BigDecimal.TEN, "USD"));

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
    void When_cancelling_payment_with_valid_order_id_should_succeed() throws StripeException {
        // Given
        String orderId = generateUniqueOrderId();

        createTestPayment(orderId);


        wireMock.stubFor(get(urlEqualTo("/v1/payment_intents/pi_test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "pi_test",
                                    "status": "required_payment_method"
                                }
                                """)));

        wireMock.stubFor(post(urlEqualTo("/v1/payment_intents/pi_test/cancel"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                             {
                                                 "id": "pi_test",
                                                 "status": "canceled"
                                             }
                                """)));

        // When
        PaymentResponse response = paymentService.cancelPayment(orderId);

        // Then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(PaymentStatus.CANCELED);

        // Verify payment status was updated in database
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    void When_refunding_payment_with_valid_order_id_should_succeed() {
        // Given
        String orderId = generateUniqueOrderId();

        createTestPayment(orderId);


        wireMock.stubFor(get(urlEqualTo("/v1/payment_intents/pi_test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "pi_test",
                                    "status": "completed"
                                }
                                """)));

        wireMock.stubFor(post(urlEqualTo("/v1/refunds"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "3214328912312312",
                                  "object": "refund",
                                  "amount": 1000,
                                  "balance_transaction": "txn_1Nispe2eZvKYlo2CYezqFhEx",
                                  "charge": "ch_1NirD82eZvKYlo2CIvbtLWuY",
                                  "created": 1692942318,
                                  "currency": "usd",
                                  "destination_details": {
                                    "card": {
                                      "reference": "123456789012",
                                      "reference_status": "available",
                                      "reference_type": "acquirer_reference_number",
                                      "type": "refund"
                                    },
                                    "type": "card"
                                  },
                                  "metadata": {},
                                  "payment_intent": "pi_test",
                                  "reason": null,
                                  "receipt_number": null,
                                  "source_transfer_reversal": null,
                                  "status": "succeeded",
                                  "transfer_reversal": null
                                }
                                """)
                ));

        // When
        PaymentResponse response = paymentService.refundPayment(orderId);

        // Then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);

        // Verify payment status was updated in database
        Payment updatedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }


    private String generateUniqueOrderId() {
        return "order-" + UUID.randomUUID().toString();
    }

    private void createTestPayment(String orderId) {

        wireMock.stubFor(post(urlEqualTo("/v1/payment_intents"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "pi_test",
                                    "client_secret": "test_secret",
                                    "status": "requires_payment_method"
                                }
                                """)));

        paymentService.createPayment(new PaymentRequest(orderId, BigDecimal.TEN, "USD"));
    }
}