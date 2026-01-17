package booking.example.demo.controller;

import booking.example.demo.dto.BookingRequest;
import booking.example.demo.dto.BookingResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import booking.example.demo.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
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
}

