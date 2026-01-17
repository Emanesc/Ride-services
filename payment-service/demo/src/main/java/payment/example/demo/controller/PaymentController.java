package payment.example.demo.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.example.demo.dto.PaymentRequest;
import payment.example.demo.dto.PaymentResponse;
import payment.example.demo.service.PaymentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            log.info("Received payment request: {}", request);
            PaymentResponse response = paymentService.createPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating payment", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        try {
            log.info("Fetching payment with id: {}", id);
            return paymentService.getPaymentById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("Payment not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error fetching payment with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBookingId(@PathVariable Long bookingId) {
        try {
            log.info("Fetching payments for booking: {}", bookingId);
            List<PaymentResponse> payments = paymentService.getPaymentsByBookingId(bookingId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching payments for booking: {}", bookingId, e);
            throw e;
        }
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByRideId(@PathVariable Long rideId) {
        try {
            log.info("Fetching payments for ride: {}", rideId);
            List<PaymentResponse> payments = paymentService.getPaymentsByRideId(rideId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching payments for ride: {}", rideId, e);
            throw e;
        }
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByPassengerId(@PathVariable String passengerId) {
        try {
            log.info("Fetching payments for passenger: {}", passengerId);
            List<PaymentResponse> payments = paymentService.getPaymentsByPassengerId(passengerId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Error fetching payments for passenger: {}", passengerId, e);
            throw e;
        }
    }
}

