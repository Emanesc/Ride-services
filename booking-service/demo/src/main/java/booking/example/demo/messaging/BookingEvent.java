package booking.example.demo.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent implements Serializable {
    private Long bookingId;
    private Long rideId;
    private Integer numberOfSeats;
    private String eventType;
}

