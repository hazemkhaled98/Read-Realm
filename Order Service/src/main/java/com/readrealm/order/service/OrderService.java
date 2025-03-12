package com.readrealm.order.service;

import com.readrealm.order.client.CatalogClient;
import com.readrealm.order.client.InventoryClient;
import com.readrealm.order.client.PaymentClient;
import com.readrealm.order.dto.Details;
import com.readrealm.order.dto.OrderRequest;
import com.readrealm.order.dto.OrderResponse;
import com.readrealm.order.mapper.OrderMapper;
import com.readrealm.order.model.Order;
import com.readrealm.order.model.OrderDetails;
import com.readrealm.order.model.backend.catalog.BookResponse;
import com.readrealm.order.model.backend.inventory.InventoryRequest;
import com.readrealm.order.model.backend.payment.PaymentRequest;
import com.readrealm.order.model.backend.payment.PaymentResponse;
import com.readrealm.order.model.backend.payment.PaymentStatus;
import com.readrealm.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;
    private final CatalogClient catalogClient;
    private final PaymentClient paymentClient;

    public OrderResponse createOrder(OrderRequest orderRequest) {

        Order order = orderRepository.save(orderMapper.toOrder(orderRequest));

        Map<String, Integer> bookIsbnToQuantityMap = orderRequest.details()
                .stream()
                .collect(toMap(Details::isbn, Details::quantity));

        reserveStock(bookIsbnToQuantityMap);

        updateOrder(order, bookIsbnToQuantityMap);

        PaymentResponse paymentResponse = processPayment(order.getOrderId(), order.getTotalAmount());


        return orderMapper.toOrderResponse(order, paymentResponse);
    }


    public OrderResponse getOrderById(String orderId) {
        return orderMapper.toOrderResponse(
                orderRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Order not found with ID: " + orderId)));
    }


    public List<OrderResponse> getOrdersByUserId(Integer userId) {
        List<OrderResponse> orders = orderMapper.toOrderResponseList(orderRepository.findByUserId(userId));
        if (orders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No orders found for user ID: " + userId);
        }
        return orders;
    }

    public OrderResponse confirmOrder(String orderId) {

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + orderId));

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order with ID: " + orderId + " is already paid");
        }

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);

        return orderMapper.toOrderResponse(order);
    }

    public OrderResponse cancelOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + orderId));

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order with ID: " + orderId + " is already paid");
        }

        PaymentResponse paymentResponse = paymentClient.cancelPayment(orderId);

        order.setPaymentStatus(PaymentStatus.CANCELED);
        orderRepository.save(order);

        return orderMapper.toOrderResponse(order, paymentResponse);
    }

    public OrderResponse refundOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found with ID: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order with ID: " + orderId + " is not paid yet");
        }

        PaymentResponse paymentResponse = paymentClient.refundPayment(orderId);

        order.setPaymentStatus(PaymentStatus.REFUNDED);
        orderRepository.save(order);

        return orderMapper.toOrderResponse(order, paymentResponse);
    }


    private void reserveStock(Map<String, Integer> bookIsbnToQuantityMap) {
        List<InventoryRequest> inventoryRequests = bookIsbnToQuantityMap
                .entrySet()
                .stream()
                .map(entry -> new InventoryRequest(entry.getKey(), entry.getValue()))
                .toList();

        inventoryClient.reserveInventory(inventoryRequests);
    }

    private void updateOrder(Order order, Map<String, Integer> bookIsbnToQuantityMap) {
        List<BookResponse> books = catalogClient.getBooksByISBNs(bookIsbnToQuantityMap.keySet());

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderDetails> orderDetails = new ArrayList<>();

        for(BookResponse book : books) {
            int quantity = bookIsbnToQuantityMap.get(book.isbn());
            BigDecimal price = book.price();
            orderDetails.add(new OrderDetails(book.isbn(), quantity, price));
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalAmount(totalAmount);
        order.setDetails(orderDetails);
        order.setPaymentStatus(PaymentStatus.PENDING);
        orderRepository.save(order);
    }

    private PaymentResponse processPayment(String orderId, BigDecimal totalAmount) {
        PaymentRequest paymentRequest = new PaymentRequest(orderId, totalAmount, "USD");

        return paymentClient.processPayment(paymentRequest);
    }

}