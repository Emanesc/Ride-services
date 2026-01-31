package booking.example.demo.service;

// import booking.example.demo.dto.RideAvailabilityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class RideServiceClient {

    private final RestTemplate restTemplate;
    private static final String RIDE_SERVICE_NAME = "ride-service";

    public RideServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public booking.example.demo.dto.RideResponse getRideById(Long rideId) {
        try {
            String url = "http://" + RIDE_SERVICE_NAME + "/api/rides/" + rideId;
            log.info("Fetching ride details for {} at {}", rideId, url);
            ResponseEntity<booking.example.demo.dto.RideResponse> response = restTemplate.getForEntity(
                    url, booking.example.demo.dto.RideResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching ride {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Failed to fetch ride details: " + e.getMessage(), e);
        }
    }

    public List<booking.example.demo.dto.RideResponse> searchRides(String city) {
        try {
            String url = "http://" + RIDE_SERVICE_NAME + "/api/rides/search?destination=" + city;
            log.info("Searching rides in {} at {}", city, url);
            // Note: Simplification - searching by destination as 'place'. Could be departure too.
            // Using parameterized type reference or array for list
            ResponseEntity<booking.example.demo.dto.RideResponse[]> response = restTemplate.getForEntity(
                    url, booking.example.demo.dto.RideResponse[].class);
            
            if (response.getBody() == null) return java.util.Collections.emptyList();
            return java.util.Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Error searching rides for city {}: {}", city, e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}

