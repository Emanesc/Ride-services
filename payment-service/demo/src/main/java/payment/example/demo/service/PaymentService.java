package payment.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.example.demo.dto.PaymentRequest;
import payment.example.demo.dto.PaymentResponse;
import payment.example.demo.model.Payment;
import payment.example.demo.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for booking {} on ride {}", request.getBookingId(), request.getRideId());

        // Calculer le montant partagé
        Double sharedAmount = calculateSharedAmount(request.getTotalAmount(), request.getNumberOfPassengers());
        
        log.info("Total amount: {}, Number of passengers: {}, Shared amount per passenger: {}", 
                request.getTotalAmount(), request.getNumberOfPassengers(), sharedAmount);

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setRideId(request.getRideId());
        payment.setPassengerId(request.getPassengerId());
        payment.setDriverId(request.getDriverId());
        payment.setTotalAmount(request.getTotalAmount());
        payment.setSharedAmount(sharedAmount);
        payment.setNumberOfPassengers(request.getNumberOfPassengers());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentTime(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Payment created successfully with id: {}", savedPayment.getId());
        
        // Simuler le traitement du paiement (normalement ici on appellerait une passerelle de paiement)
        processPayment(savedPayment);
        
        return mapToResponse(savedPayment);
    }

    private Double calculateSharedAmount(Double totalAmount, Integer numberOfPassengers) {
        if (numberOfPassengers == null || numberOfPassengers <= 0) {
            throw new IllegalArgumentException("Number of passengers must be greater than 0");
        }
        // Le montant partagé = total / nombre de passagers (y compris le conducteur dans certains cas)
        // Ici on considère que le conducteur ne paie pas, donc on divise par le nombre de passagers
        return Math.round((totalAmount / numberOfPassengers) * 100.0) / 100.0;
    }

    private void processPayment(Payment payment) {
        // Simuler un traitement de paiement
        // Dans un vrai système, on appellerait une passerelle de paiement (Stripe, PayPal, etc.)
        try {
            log.info("Processing payment {} for amount {}", payment.getId(), payment.getSharedAmount());
            // Simuler un délai de traitement
            Thread.sleep(100);
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            log.info("Payment {} processed successfully", payment.getId());
        } catch (Exception e) {
            log.error("Error processing payment {}: {}", payment.getId(), e.getMessage());
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    public Optional<PaymentResponse> getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByRideId(Long rideId) {
        return paymentRepository.findByRideId(rideId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByPassengerId(String passengerId) {
        return paymentRepository.findByPassengerId(passengerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .driverId(payment.getDriverId())
                .totalAmount(payment.getTotalAmount())
                .sharedAmount(payment.getSharedAmount())
                .numberOfPassengers(payment.getNumberOfPassengers())
                .status(payment.getStatus())
                .paymentTime(payment.getPaymentTime())
                .build();
    }
}

