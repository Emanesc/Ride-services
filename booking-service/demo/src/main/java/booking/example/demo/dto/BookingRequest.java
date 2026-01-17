package booking.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequest {
    
    @NotNull(message = "Ride ID is required")
    private Long rideId;
    
    @NotBlank(message = "Passenger ID is required")
    private String passengerId;
    
    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat must be booked")
    @Max(value = 10, message = "Maximum 10 seats allowed per booking")
    private Integer numberOfSeats;
}

