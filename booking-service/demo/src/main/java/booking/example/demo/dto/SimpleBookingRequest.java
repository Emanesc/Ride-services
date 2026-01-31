package booking.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SimpleBookingRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
}

