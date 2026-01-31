package payment.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerAmountResponse {
    private Long bookingId;
    private Long rideId;
    private String passengerId;
    private Integer numberOfSeats;
    private Double totalRidePrice;
    private Integer totalPassengers;
    private Double amountPerPassenger;
    private Double totalAmountForThisPassenger;
}

