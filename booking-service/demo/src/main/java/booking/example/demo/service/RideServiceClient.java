package booking.example.demo.service;

import booking.example.demo.dto.RideAvailabilityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RideServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${ride.service.url:http://localhost:8081}")
    private String rideServiceUrl;

    public RideServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RideAvailabilityResponse checkAvailability(Long rideId) {
        try {
            log.info("Checking availability for ride {} at {}", rideId, rideServiceUrl);
            String url = rideServiceUrl + "/api/rides/" + rideId + "/availability";
            ResponseEntity<RideAvailabilityResponse> response = restTemplate.getForEntity(
                    url, RideAvailabilityResponse.class);
            log.info("Availability response for ride {}: {}", rideId, response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error checking availability for ride {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Failed to check ride availability: " + e.getMessage(), e);
        }
    }

    public void updateRideSeats(Long rideId, int seatsToReserve) {
        try {
            log.info("Updating seats for ride {}: reserving {} seats", rideId, seatsToReserve);
            // This would need a dedicated endpoint in ride-service
            // For now, we'll handle it through the booking service logic
        } catch (Exception e) {
            log.error("Error updating seats for ride {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Failed to update ride seats: " + e.getMessage(), e);
        }
    }
}

