package booking.example.demo.service;

import booking.example.demo.dto.BookingRequest;
import booking.example.demo.dto.BookingResponse;
import booking.example.demo.dto.RideAvailabilityResponse;
import booking.example.demo.messaging.BookingEventPublisher;
import booking.example.demo.model.Booking;
import booking.example.demo.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RideServiceClient rideServiceClient;
    private final BookingEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository, 
                         RideServiceClient rideServiceClient,
                         BookingEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.rideServiceClient = rideServiceClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for ride {} by passenger {} for {} seats", 
                request.getRideId(), request.getPassengerId(), request.getNumberOfSeats());

        // Vérifier la disponibilité via ride-service
        RideAvailabilityResponse availability = checkAvailability(request.getRideId());
        
        if (availability == null || !availability.getAvailable()) {
            throw new IllegalArgumentException("Ride is not available");
        }

        if (availability.getAvailableSeats() < request.getNumberOfSeats()) {
            throw new IllegalArgumentException(
                    "Not enough available seats. Available: " + availability.getAvailableSeats() + 
                    ", Requested: " + request.getNumberOfSeats());
        }

        // Créer la réservation
        Booking booking = new Booking();
        booking.setRideId(request.getRideId());
        booking.setPassengerId(request.getPassengerId());
        booking.setNumberOfSeats(request.getNumberOfSeats());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setBookingTime(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        
        log.info("Booking created successfully with id: {}", savedBooking.getId());
        
        // Publier un événement asynchrone pour mettre à jour les sièges disponibles
        eventPublisher.publishBookingCreated(
                savedBooking.getId(), 
                savedBooking.getRideId(), 
                savedBooking.getNumberOfSeats());
        
        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse processBookingFromId(Long bookingId) {
        log.info("Processing booking from ID: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is cancelled");
        }
        
        // Vérifier la disponibilité via ride-service
        RideAvailabilityResponse availability = checkAvailability(booking.getRideId());
        
        if (availability == null || !availability.getAvailable()) {
            throw new IllegalArgumentException("Ride is not available");
        }

        if (availability.getAvailableSeats() < booking.getNumberOfSeats()) {
            throw new IllegalArgumentException(
                    "Not enough available seats. Available: " + availability.getAvailableSeats() + 
                    ", Requested: " + booking.getNumberOfSeats());
        }
        
        // Publier un événement asynchrone pour mettre à jour les sièges disponibles
        eventPublisher.publishBookingCreated(
                booking.getId(), 
                booking.getRideId(), 
                booking.getNumberOfSeats());
        
        return mapToResponse(booking);
    }

    public Optional<BookingResponse> getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<BookingResponse> getBookingsByRideId(Long rideId) {
        return bookingRepository.findByRideId(rideId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsByPassengerId(String passengerId) {
        return bookingRepository.findByPassengerId(passengerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                        throw new IllegalArgumentException("Booking is already cancelled");
                    }
                    booking.setStatus(Booking.BookingStatus.CANCELLED);
                    Booking cancelledBooking = bookingRepository.save(booking);
                    log.info("Booking {} cancelled", id);
                    return mapToResponse(cancelledBooking);
                })
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + id));
    }

    public List<BookingResponse> getBookingsByCity(String city) {
        log.info("Searching bookings by city: {}", city);
        
        // 1. Get rides for this city
        List<booking.example.demo.dto.RideResponse> rides = rideServiceClient.searchRides(city);
        
        if (rides.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<Long> rideIds = rides.stream()
                .map(booking.example.demo.dto.RideResponse::getId)
                .collect(Collectors.toList());
        
        // 2. Find bookings for these ride IDs
        return bookingRepository.findByRideIdIn(rideIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RideAvailabilityResponse checkAvailability(Long rideId) {
        // 1. Get Ride Details
        booking.example.demo.dto.RideResponse ride = rideServiceClient.getRideById(rideId);
        
        if (ride == null) {
            throw new IllegalArgumentException("Ride not found");
        }
        
        // 2. Count sold seats
        Integer soldSeats = bookingRepository.sumSeatsByRideIdAndStatus(rideId, Booking.BookingStatus.CONFIRMED);
        if (soldSeats == null) soldSeats = 0;
        
        // 3. Calculate available seats locally
        int localAvailable = ride.getAvailableSeats() - soldSeats;
        
        // Use the minimum between local calculation and ride-service data for safety
        int finalAvailable = Math.min(localAvailable, ride.getAvailableSeats());
        
        log.debug("Ride {}: local calculation={}, ride-service={}, using={}", 
                  rideId, localAvailable, ride.getAvailableSeats(), finalAvailable);
        
        // 4. Build response
        RideAvailabilityResponse response = new RideAvailabilityResponse();
        response.setRideId(rideId);
        response.setAvailable(finalAvailable > 0);
        response.setAvailableSeats(finalAvailable);
        response.setMessage(finalAvailable > 0 ? "Available" : "Full");
        response.setPrice(ride.getPrice());
        
        return response;
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .rideId(booking.getRideId())
                .passengerId(booking.getPassengerId())
                .numberOfSeats(booking.getNumberOfSeats())
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .build();
    }

    // ========================================
    // NOUVELLES MÉTHODES POUR LE SYSTÈME D'ÉVALUATION
    // ========================================

    /**
     * Récupérer tous les bookings (pour le dropdown)
     */
    public List<BookingResponse> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les informations complètes pour l'évaluation
     */
    public Map<String, Object> getEvaluationInfo(Long bookingId) {
        log.info("Fetching evaluation info for booking: {}", bookingId);
        
        // 1. Récupérer le booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        
        // 2. Récupérer les infos du ride via RideServiceClient
        booking.example.demo.dto.RideResponse ride = rideServiceClient.getRideById(booking.getRideId());
        
        // 3. Construire la réponse
        Map<String, Object> evaluationInfo = new HashMap<>();
        evaluationInfo.put("bookingId", booking.getId());
        evaluationInfo.put("rideId", booking.getRideId());
        evaluationInfo.put("passengerId", booking.getPassengerId());
        
        // Infos du driver depuis le ride
        if (ride != null) {
            evaluationInfo.put("driverId", ride.getDriverId());
            evaluationInfo.put("driverName", ride.getDriverName());
        } else {
            evaluationInfo.put("driverId", "UNKNOWN");
            evaluationInfo.put("driverName", "Unknown Driver");
        }
        
        log.info("Evaluation info prepared for booking {}", bookingId);
        return evaluationInfo;
    }

    /**
     * Supprimer définitivement un booking
     */
    @Transactional
    public void deleteBooking(Long id) {
        log.info("Deleting booking: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + id));
        
        bookingRepository.delete(booking);
        log.info("Booking deleted successfully: {}", id);
    }
}