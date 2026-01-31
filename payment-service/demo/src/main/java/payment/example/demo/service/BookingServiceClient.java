package payment.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import payment.example.demo.dto.BookingInfo;

@Slf4j
@Service
public class BookingServiceClient {

    private final RestTemplate restTemplate;
    private static final String BOOKING_SERVICE_NAME = "booking-service";

    public BookingServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BookingInfo getBookingById(Long bookingId) {
        try {
            String url = "http://" + BOOKING_SERVICE_NAME + "/api/bookings/" + bookingId;
            log.info("Fetching booking {} from {}", bookingId, url);
            ResponseEntity<BookingInfo> response = restTemplate.getForEntity(url, BookingInfo.class);
            log.info("Booking info retrieved: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching booking {}: {}", bookingId, e.getMessage());
            throw new RuntimeException("Failed to fetch booking: " + e.getMessage(), e);
        }
    }
}

