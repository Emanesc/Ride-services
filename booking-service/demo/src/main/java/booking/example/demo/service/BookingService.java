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
import java.util.List;
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
        RideAvailabilityResponse availability = rideServiceClient.checkAvailability(request.getRideId());
        
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
}

