package booking.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rideId;
    private String passengerId;
    private Integer numberOfSeats;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    
    private LocalDateTime bookingTime;
    
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED
    }
}

