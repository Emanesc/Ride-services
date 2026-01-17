package ride.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ride.example.demo.model.Ride;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByDepartureContainingIgnoreCase(String departure);
    List<Ride> findByDestinationContainingIgnoreCase(String destination);
    List<Ride> findByDepartureContainingIgnoreCaseAndDestinationContainingIgnoreCase(
            String departure, String destination);
}
