package ride.example.demo.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ride.example.demo.dto.AvailabilityResponse;
import ride.example.demo.dto.RideRequest;
import ride.example.demo.dto.RideResponse;
import ride.example.demo.model.Ride;
import ride.example.demo.service.RideService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody RideRequest request) {
        try {
            log.info("Creating ride from {} to {} at {}", request.getDeparture(), 
                    request.getDestination(), request.getDepartureTime());
            
            Ride ride = new Ride();
            ride.setDeparture(request.getDeparture());
            ride.setDestination(request.getDestination());
            ride.setDepartureTime(request.getDepartureTime());
            ride.setAvailableSeats(request.getAvailableSeats());
            ride.setPrice(request.getPrice());

            Ride savedRide = rideService.createRide(ride);
            
            RideResponse response = mapToResponse(savedRide);
            log.info("Ride created successfully with id: {}", savedRide.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating ride", e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<RideResponse>> getAllRides() {
        try {
            log.info("Fetching all rides");
            List<Ride> rides = rideService.getAllRides();
            List<RideResponse> responses = rides.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            log.info("Found {} rides", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching rides", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRideById(@PathVariable Long id) {
        try {
            log.info("Fetching ride with id: {}", id);
            return rideService.getRideById(id)
                    .map(ride -> {
                        log.info("Ride found: {}", ride.getId());
                        return ResponseEntity.ok(mapToResponse(ride));
                    })
                    .orElseGet(() -> {
                        log.warn("Ride not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error fetching ride with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(@PathVariable Long id) {
        try {
            log.info("Checking availability for ride id: {}", id);
            return rideService.getRideById(id)
                    .map(ride -> {
                        boolean available = ride.getAvailableSeats() > 0;
                        AvailabilityResponse response = new AvailabilityResponse(
                                ride.getId(), 
                                available, 
                                ride.getAvailableSeats()
                        );
                        log.info("Ride {} availability: {} ({} seats)", 
                                id, available, ride.getAvailableSeats());
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        log.warn("Ride not found for availability check: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error checking availability for ride id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<RideResponse>> searchRides(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination) {
        try {
            log.info("Searching rides - departure: {}, destination: {}", departure, destination);
            List<Ride> rides = rideService.searchRides(departure, destination);
            List<RideResponse> responses = rides.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            log.info("Found {} rides matching criteria", responses.size());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error searching rides", e);
            throw e;
        }
    }

    private RideResponse mapToResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .departure(ride.getDeparture())
                .destination(ride.getDestination())
                .departureTime(ride.getDepartureTime())
                .availableSeats(ride.getAvailableSeats())
                .price(ride.getPrice())
                .build();
    }
}

