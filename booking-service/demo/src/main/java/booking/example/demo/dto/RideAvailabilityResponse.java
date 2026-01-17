package booking.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideAvailabilityResponse {
    private Long rideId;
    private Boolean available;
    private Integer availableSeats;
}

