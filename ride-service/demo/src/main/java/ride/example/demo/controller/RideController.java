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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    /**
     * NOUVEAU : Liste des drivers statiques
     */
    @GetMapping("/drivers")
    public ResponseEntity<List<Map<String, Object>>> getDrivers() {
        try {
            log.info("Fetching available drivers");
            
            List<Map<String, Object>> drivers = new ArrayList<>();
            
            // Driver 1
            Map<String, Object> driver1 = new HashMap<>();
            driver1.put("id", "DRV001");
            driver1.put("name", "Ahmed Ben Ali");
            driver1.put("rating", 4.8);
            driver1.put("totalRides", 156);
            driver1.put("phone", "+222 45 12 34 56");
            drivers.add(driver1);
            
            // Driver 2
            Map<String, Object> driver2 = new HashMap<>();
            driver2.put("id", "DRV002");
            driver2.put("name", "Fatima Mohamed");
            driver2.put("rating", 4.9);
            driver2.put("totalRides", 203);
            driver2.put("phone", "+222 45 23 45 67");
            drivers.add(driver2);
            
            // Driver 3
            Map<String, Object> driver3 = new HashMap<>();
            driver3.put("id", "DRV003");
            driver3.put("name", "Omar Sidibe");
            driver3.put("rating", 4.7);
            driver3.put("totalRides", 98);
            driver3.put("phone", "+222 45 34 56 78");
            drivers.add(driver3);
            
            // Driver 4
            Map<String, Object> driver4 = new HashMap<>();
            driver4.put("id", "DRV004");
            driver4.put("name", "Aicha Diallo");
            driver4.put("rating", 4.6);
            driver4.put("totalRides", 124);
            driver4.put("phone", "+222 45 45 67 89");
            drivers.add(driver4);
            
            // Driver 5
            Map<String, Object> driver5 = new HashMap<>();
            driver5.put("id", "DRV005");
            driver5.put("name", "Youssef Kane");
            driver5.put("rating", 4.9);
            driver5.put("totalRides", 187);
            driver5.put("phone", "+222 45 56 78 90");
            drivers.add(driver5);
            
            // Driver 6
            Map<String, Object> driver6 = new HashMap<>();
            driver6.put("id", "DRV006");
            driver6.put("name", "Mariam Sow");
            driver6.put("rating", 4.8);
            driver6.put("totalRides", 142);
            driver6.put("phone", "+222 45 67 89 01");
            drivers.add(driver6);
            
            // Driver 7
            Map<String, Object> driver7 = new HashMap<>();
            driver7.put("id", "DRV007");
            driver7.put("name", "Ibrahim Sy");
            driver7.put("rating", 4.7);
            driver7.put("totalRides", 93);
            driver7.put("phone", "+222 45 78 90 12");
            drivers.add(driver7);
            
            log.info("Returning {} drivers", drivers.size());
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            log.error("Error fetching drivers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody RideRequest request) {
        try {
            log.info("Creating ride from {} to {} at {} with driver: {}", 
                    request.getDeparture(), 
                    request.getDestination(), 
                    request.getDepartureTime(),
                    request.getDriverId());
            
            Ride ride = new Ride();
            ride.setDeparture(request.getDeparture());
            ride.setDestination(request.getDestination());
            ride.setDepartureTime(request.getDepartureTime());
            ride.setAvailableSeats(request.getAvailableSeats());
            ride.setPrice(request.getPrice());
            
            // Set driver information
            ride.setDriverId(request.getDriverId());
            ride.setDriverName(request.getDriverName());
            if (request.getEventId() != null) {
                ride.setEventId(request.getEventId());
            }

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
                        boolean available = ride.isServiceAvailable();
                        String message = ride.getAvailabilityMessage();
                        
                        AvailabilityResponse response = new AvailabilityResponse(
                                ride.getId(), 
                                available, 
                                ride.getAvailableSeats(),
                                message,
                                ride.getPrice()
                        );
                        log.info("Ride {} availability: {} - {}", 
                                id, available, message);
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

    /**
     * NOUVEAU : Supprimer un ride
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRide(@PathVariable Long id) {
        try {
            log.info("Deleting ride with id: {}", id);
            
            // Vérifier si le ride existe
            Optional<Ride> rideOpt = rideService.getRideById(id);
            if (!rideOpt.isPresent()) {
                log.warn("Ride not found with id: {}", id);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ride not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Supprimer le ride
            rideService.deleteRide(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ride deleted successfully");
            response.put("id", id.toString());
            
            log.info("Ride deleted successfully: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting ride: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error deleting ride: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
                .driverId(ride.getDriverId())
                .driverName(ride.getDriverName())
                .eventId(ride.getEventId())
                .build();
    }
}