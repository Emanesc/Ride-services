package booking.example.demo.controller;

import booking.example.demo.dto.BookingRequest;
import booking.example.demo.dto.BookingResponse;
import booking.example.demo.dto.RideAvailabilityResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import booking.example.demo.service.BookingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", allowedHeaders = "*", 
    methods = {RequestMethod.GET, RequestMethod.POST, 
               RequestMethod.PUT, RequestMethod.DELETE, 
               RequestMethod.OPTIONS})
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        try {
            log.info("Received booking request: {}", request);
            BookingResponse response = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid booking request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating booking", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        try {
            log.info("Fetching booking with id: {}", id);
            return bookingService.getBookingById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("Booking not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error fetching booking with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRideId(@PathVariable Long rideId) {
        try {
            log.info("Fetching bookings for ride: {}", rideId);
            List<BookingResponse> bookings = bookingService.getBookingsByRideId(rideId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching bookings for ride: {}", rideId, e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookingResponse>> getBookingsByCity(@RequestParam String city) {
        try {
            log.info("Fetching bookings for city: {}", city);
            List<BookingResponse> bookings = bookingService.getBookingsByCity(city);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching bookings for city: {}", city, e);
            throw e;
        }
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByPassengerId(@PathVariable String passengerId) {
        try {
            log.info("Fetching bookings for passenger: {}", passengerId);
            List<BookingResponse> bookings = bookingService.getBookingsByPassengerId(passengerId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching bookings for passenger: {}", passengerId, e);
            throw e;
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        try {
            log.info("Cancelling booking: {}", id);
            BookingResponse response = bookingService.cancelBooking(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cancellation request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/process/{bookingId}")
    public ResponseEntity<BookingResponse> processBooking(@PathVariable Long bookingId) {
        try {
            log.info("Processing booking with ID: {}", bookingId);
            BookingResponse response = bookingService.processBookingFromId(bookingId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid booking request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing booking: {}", bookingId, e);
            throw e;
        }
    }

    @GetMapping("/availability/{rideId}")
    public ResponseEntity<RideAvailabilityResponse> checkAvailability(@PathVariable Long rideId) {
        try {
            log.info("Checking availability for ride: {}", rideId);
            RideAvailabilityResponse response = bookingService.checkAvailability(rideId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking availability for ride: {}", rideId, e);
            throw e;
        }
    }

    // ========================================
    // NOUVELLES MÉTHODES POUR LE SYSTÈME D'ÉVALUATION
    // ========================================

    /**
     * Liste de tous les bookings pour le dropdown d'évaluation
     */
    @GetMapping("/all")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        try {
            log.info("Fetching all bookings for evaluation dropdown");
            List<BookingResponse> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching all bookings", e);
            throw e;
        }
    }

    /**
     * Récupérer les informations complètes pour remplir le formulaire d'évaluation
     */
    @GetMapping("/{id}/evaluation-info")
    public ResponseEntity<Map<String, Object>> getEvaluationInfo(@PathVariable Long id) {
        try {
            log.info("Fetching evaluation info for booking id: {}", id);
            Map<String, Object> evaluationInfo = bookingService.getEvaluationInfo(id);
            return ResponseEntity.ok(evaluationInfo);
        } catch (IllegalArgumentException e) {
            log.warn("Booking not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching evaluation info for booking: {}", id, e);
            throw e;
        }
    }

    /**
     * Supprimer définitivement un booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBooking(@PathVariable Long id) {
        try {
            log.info("Deleting booking with id: {}", id);
            bookingService.deleteBooking(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Booking deleted successfully");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Booking not found: {}", id);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Booking not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            log.error("Error deleting booking: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error deleting booking: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}