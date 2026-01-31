package payment.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import payment.example.demo.dto.RideInfo;

@Slf4j
@Service
public class RideServiceClient {

    private final RestTemplate restTemplate;
    private static final String RIDE_SERVICE_NAME = "ride-service";

    public RideServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RideInfo getRideById(Long rideId) {
        try {
            String url = "http://" + RIDE_SERVICE_NAME + "/api/rides/" + rideId;
            log.info("Fetching ride {} from {}", rideId, url);
            ResponseEntity<RideInfo> response = restTemplate.getForEntity(url, RideInfo.class);
            log.info("Ride info retrieved: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching ride {}: {}", rideId, e.getMessage());
            throw new RuntimeException("Failed to fetch ride: " + e.getMessage(), e);
        }
    }
}

