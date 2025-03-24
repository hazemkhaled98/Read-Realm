package com.readrealm.notification.service;

import com.readrealm.order.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {


    private final JavaMailSender javaMailSender;


    @KafkaListener(topics = "orders")
    public void confirmOrder(OrderEvent orderEvent) {

        String orderId = orderEvent.getOrderId();

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("customer-support@readrealm.com");
            messageHelper.setTo(orderEvent.getUserEmail());
            messageHelper.setSubject(String.format("Your Order with OrderNumber %s is placed successfully", orderId));
            messageHelper.setText(String.format("""
                            Hi %s %s

                            Your order with order id %s is now %s. Thanks for shopping with us.

                            Best Regards
                            ReadRealm Team
                            """,
                    orderEvent.getUserFirstName(),
                    orderEvent.getUserLastName(),
                    orderId,
                    orderEvent.getPaymentStatus()));
        };
        try {
            javaMailSender.send(messagePreparator);
            log.info("Notification email is sent for order id: {}", orderId);
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
