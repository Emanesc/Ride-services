package ride.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RideRequest {
    
    @NotBlank(message = "Departure location is required")
    private String departure;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;
    
    @NotNull(message = "Available seats is required")
    @Min(value = 1, message = "At least 1 seat must be available")
    @Max(value = 10, message = "Maximum 10 seats allowed")
    private Integer availableSeats;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
}

