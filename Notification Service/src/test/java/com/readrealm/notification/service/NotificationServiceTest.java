package com.readrealm.notification.service;

import com.readrealm.order.event.OrderDetails;
import com.readrealm.order.event.OrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.readrealm.order.event.PaymentStatus.COMPLETED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "orders" })
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("Notification Service Integration Test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NotificationServiceTest {

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private NotificationService notificationService;

    private OrderEvent orderEvent;


    @BeforeEach
    void setUp() {
        orderEvent = new OrderEvent();
        orderEvent.setPaymentStatus(COMPLETED);
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
    }

    @Test
    void Should_send_email_notification_when_order_event_is_received() throws Exception {

        doNothing().when(mailSender).send(any(MimeMessagePreparator.class));

        kafkaTemplate.send("orders", orderEvent.getOrderId(), orderEvent);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
        });
    }

    @Test
    void Should_throw_exception_when_mail_fail_to_be_sent() {

        doThrow(mock(MailException.class))
                .when(mailSender).send(any(MimeMessagePreparator.class));

        assertThatThrownBy(() -> notificationService.confirmOrder(orderEvent))
                .isInstanceOf(ResponseStatusException.class);

        verify(mailSender, times(1)).send(any(MimeMessagePreparator.class));
    }
}