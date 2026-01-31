package booking.example.demo.repository;

import booking.example.demo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRideId(Long rideId);
    List<Booking> findByPassengerId(String passengerId);
    List<Booking> findByStatus(Booking.BookingStatus status);
    List<Booking> findByRideIdIn(List<Long> rideIds);
    
    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.numberOfSeats) FROM Booking b WHERE b.rideId = :rideId AND b.status = :status")
    Integer sumSeatsByRideIdAndStatus(Long rideId, Booking.BookingStatus status);

}