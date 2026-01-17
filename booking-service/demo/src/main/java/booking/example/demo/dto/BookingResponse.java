package booking.example.demo.dto;

import booking.example.demo.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long rideId;
    private String passengerId;
    private Integer numberOfSeats;
    private Booking.BookingStatus status;
    private LocalDateTime bookingTime;
}

