package payment.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentByBookingRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @NotNull(message = "Driver ID is required")
    private String driverId;
}

