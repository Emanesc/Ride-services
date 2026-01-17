package payment.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @NotNull(message = "Ride ID is required")
    private Long rideId;
    
    @NotBlank(message = "Passenger ID is required")
    private String passengerId;
    
    @NotBlank(message = "Driver ID is required")
    private String driverId;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;
    
    @NotNull(message = "Number of passengers is required")
    @Min(value = 1, message = "At least 1 passenger required")
    private Integer numberOfPassengers;
}

