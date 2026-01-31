package payment.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideInfo {
    private Long id;
    private String departure;
    private String destination;
    private LocalDateTime departureTime;
    private Integer availableSeats;
    private Double price;
}

