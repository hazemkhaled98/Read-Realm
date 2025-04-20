package com.readrealm.order.service;

import com.readrealm.auth.util.SecurityUtil;
import com.readrealm.order.client.CatalogClient;
import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.event.OrderEvent;
import com.readrealm.order.mapper.OrderMapper;
import com.readrealm.order.model.Order;
import com.readrealm.order.model.OrderItem;
import com.readrealm.order.model.PaymentStatus;
import com.readrealm.order.model.catalog.BookResponse;
import com.readrealm.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CatalogClient catalogClient;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @PreAuthorize("@authorizer.isCustomer()")
    public String createOrder(OrderRequest orderRequest) {

        Order order = orderMapper.toOrder(orderRequest);

        Order processedOrder = processOrder(order);

        publishOrderEvent(processedOrder, "inventory");

        return "Order created with ID: " + processedOrder.getOrderId();
    }


    @PostAuthorize("@authorizer.isAuthorizedUser(returnObject.userId)")
    public OrderResponse getOrderById(String orderId) {
        return orderMapper.toOrderResponse(
                orderRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Order not found with ID: " + orderId)));
    }


    @PreAuthorize("@authorizer.isAuthorizedUser(#userId)")
    public List<OrderResponse> getOrdersByUserId(String userId) {
        List<OrderResponse> orders = orderMapper.toOrderResponseList(orderRepository.findByUserId(userId));
        if (orders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No orders found for user ID: " + userId);
        }
        return orders;
    }

    @KafkaListener(topics = "orders")
    public void processOrderEvents(OrderEvent orderEvent) {

        String orderId = orderEvent.getOrderId();

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + orderId));


        order.setPaymentStatus(PaymentStatus.fromString(orderEvent.getPaymentStatus().toString()));

        orderRepository.save(order);

        logOrder(orderId, order.getPaymentStatus());
    }

    @PreAuthorize("@authorizer.isCustomer()")
    public String cancelOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + orderId));

        if(!order.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new AccessDeniedException("You are not authorized to cancel this order");
        }

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order with ID: " + orderId + " is already paid");
        }

        publishOrderEvent(order, "order-cancellation");

        return "Order cancellation request is sent Successfully";
    }

    @PreAuthorize("@authorizer.isCustomer()")
    public String refundOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + orderId));

        if(!order.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new AccessDeniedException("You are not authorized to refund this order");
        }

        if (order.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order with ID: " + orderId + " is not paid yet");
        }

        publishOrderEvent(order, "order-refund");

        return "Order refund Request is sent successfully";
    }

    private Order processOrder(Order order) {

        Map<String, OrderItem> orderItems = order.getOrderItems()
                .stream()
                .collect(toMap(OrderItem::getIsbn, Function.identity()));

        List<BookResponse> books = catalogClient.getBooksByISBNs(orderItems.keySet());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for(BookResponse book : books) {
            OrderItem orderItem = orderItems.get(book.isbn());
            BigDecimal price = book.price();
            orderItem.setUnitPrice(price);
            int quantity = orderItem.getQuantity();
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        order.setUserId(SecurityUtil.getCurrentUserId());
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus(PaymentStatus.PROCESSING);
        return orderRepository.save(order);
    }

    private void publishOrderEvent(Order order, String... topics) {
        OrderEvent orderEvent = orderMapper.toOrderEvent(order, SecurityUtil.getSecurityPrincipal());

        logOrder(order.getOrderId(), order.getPaymentStatus());

        for (String topic : topics) {
            kafkaTemplate.send(topic, orderEvent);
        }
    }

    private static void logOrder(String orderId, PaymentStatus paymentStatus){
        log.info("Processing Order with ID: {}. Payment Status is {}", orderId, paymentStatus);
    }

}