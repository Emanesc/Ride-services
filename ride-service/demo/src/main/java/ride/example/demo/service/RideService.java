package ride.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ride.example.demo.model.Ride;
import ride.example.demo.repository.RideRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RideService {
    
    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional
    public Ride createRide(Ride ride) {
        log.debug("Saving ride: {}", ride);
        return rideRepository.save(ride);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    public Optional<Ride> getRideById(Long id) {
        return rideRepository.findById(id);
    }

    public boolean checkAvailability(Long rideId) {
        return rideRepository.findById(rideId)
                .map(ride -> ride.getAvailableSeats() > 0)
                .orElse(false);
    }

    public List<Ride> searchRides(String departure, String destination) {
        if (departure != null && destination != null) {
            return rideRepository.findByDepartureContainingIgnoreCaseAndDestinationContainingIgnoreCase(
                    departure, destination);
        } else if (departure != null) {
            return rideRepository.findByDepartureContainingIgnoreCase(departure);
        } else if (destination != null) {
            return rideRepository.findByDestinationContainingIgnoreCase(destination);
        }
        return getAllRides();
    }

    @Transactional
    public Ride updateAvailableSeats(Long rideId, int seatsToReserve) {
        return rideRepository.findById(rideId)
                .map(ride -> {
                    if (ride.getAvailableSeats() >= seatsToReserve) {
                        ride.setAvailableSeats(ride.getAvailableSeats() - seatsToReserve);
                        log.info("Updated available seats for ride {}: {} -> {}", 
                                rideId, ride.getAvailableSeats() + seatsToReserve, ride.getAvailableSeats());
                        return rideRepository.save(ride);
                    } else {
                        throw new IllegalArgumentException(
                                "Not enough available seats. Requested: " + seatsToReserve + 
                                ", Available: " + ride.getAvailableSeats());
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("Ride not found with id: " + rideId));
    }
}
