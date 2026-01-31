package payment.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfo {
    private Long id;
    private Long rideId;
    private String passengerId;
    private Integer numberOfSeats;
    private String status;
}

