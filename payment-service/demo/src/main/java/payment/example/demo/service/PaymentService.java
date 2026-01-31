package payment.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.example.demo.dto.*;
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
    private final BookingServiceClient bookingServiceClient;
    private final RideServiceClient rideServiceClient;

    public PaymentService(PaymentRepository paymentRepository,
                         BookingServiceClient bookingServiceClient,
                         RideServiceClient rideServiceClient) {
        this.paymentRepository = paymentRepository;
        this.bookingServiceClient = bookingServiceClient;
        this.rideServiceClient = rideServiceClient;
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

    // Nouvelle méthode pour créer un paiement à partir d'un booking ID seulement
    @Transactional
    public PaymentResponse createPaymentFromBookingId(PaymentByBookingRequest request) {
        log.info("Creating payment from booking ID: {}", request.getBookingId());

        // Récupérer les informations du booking
        BookingInfo bookingInfo = bookingServiceClient.getBookingById(request.getBookingId());
        if (bookingInfo == null) {
            throw new IllegalArgumentException("Booking not found with id: " + request.getBookingId());
        }

        // Récupérer les informations du ride
        RideInfo rideInfo = rideServiceClient.getRideById(bookingInfo.getRideId());
        if (rideInfo == null) {
            throw new IllegalArgumentException("Ride not found with id: " + bookingInfo.getRideId());
        }

        // Compter le nombre total de passagers (toutes les réservations confirmées pour ce ride)
        // Pour simplifier, on utilise le nombre de sièges réservés dans cette réservation
        // Dans un vrai système, on compterait toutes les réservations confirmées
        Integer totalPassengers = bookingInfo.getNumberOfSeats();

        // Calculer le montant partagé
        Double sharedAmount = calculateSharedAmount(rideInfo.getPrice(), totalPassengers);

        log.info("Total ride price: {}, Number of passengers: {}, Shared amount per passenger: {}",
                rideInfo.getPrice(), totalPassengers, sharedAmount);

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setRideId(bookingInfo.getRideId());
        payment.setPassengerId(bookingInfo.getPassengerId());
        payment.setDriverId(request.getDriverId());
        payment.setTotalAmount(rideInfo.getPrice());
        payment.setSharedAmount(sharedAmount);
        payment.setNumberOfPassengers(totalPassengers);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentTime(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully with id: {}", savedPayment.getId());

        // Traiter le paiement
        processPayment(savedPayment);

        return mapToResponse(savedPayment);
    }

    // Méthode pour obtenir le montant spécifique pour un passager à partir de l'ID de booking
    public PassengerAmountResponse getPassengerAmount(Long bookingId) {
        log.info("Calculating passenger amount for booking: {}", bookingId);

        // Récupérer les informations du booking
        BookingInfo bookingInfo = bookingServiceClient.getBookingById(bookingId);
        if (bookingInfo == null) {
            throw new IllegalArgumentException("Booking not found with id: " + bookingId);
        }

        // Récupérer les informations du ride
        RideInfo rideInfo = rideServiceClient.getRideById(bookingInfo.getRideId());
        if (rideInfo == null) {
            throw new IllegalArgumentException("Ride not found with id: " + bookingInfo.getRideId());
        }

        // Compter le nombre total de passagers (toutes les réservations confirmées)
        // Pour simplifier, on utilise le nombre de sièges de cette réservation
        Integer totalPassengers = bookingInfo.getNumberOfSeats();
        Double amountPerPassenger = calculateSharedAmount(rideInfo.getPrice(), totalPassengers);
        Double totalAmountForThisPassenger = amountPerPassenger * bookingInfo.getNumberOfSeats();

        return PassengerAmountResponse.builder()
                .bookingId(bookingId)
                .rideId(bookingInfo.getRideId())
                .passengerId(bookingInfo.getPassengerId())
                .numberOfSeats(bookingInfo.getNumberOfSeats())
                .totalRidePrice(rideInfo.getPrice())
                .totalPassengers(totalPassengers)
                .amountPerPassenger(amountPerPassenger)
                .totalAmountForThisPassenger(totalAmountForThisPassenger)
                .build();
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

