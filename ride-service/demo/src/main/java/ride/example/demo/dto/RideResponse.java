package ride.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {
    private Long id;
    private String departure;
    private String destination;
    private LocalDateTime departureTime;
    private Integer availableSeats;
    private Double price;
    
    // ✅ NOUVEAUX CHAMPS - Informations du driver
    private String driverId;
    private String driverName;
    
    private String eventId;  // Optionnel

    
}