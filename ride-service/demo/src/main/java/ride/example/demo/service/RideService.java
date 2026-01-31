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
        log.info("Creating ride from {} to {}", ride.getDeparture(), ride.getDestination());
        Ride savedRide = rideRepository.save(ride);
        log.info("Ride created with id: {}", savedRide.getId());
        return savedRide;
    }

    public List<Ride> getAllRides() {
        log.info("Fetching all rides");
        return rideRepository.findAll();
    }

    public Optional<Ride> getRideById(Long id) {
        log.info("Fetching ride with id: {}", id);
        return rideRepository.findById(id);
    }

    public List<Ride> searchRides(String departure, String destination) {
        log.info("Searching rides with departure: {} and destination: {}", departure, destination);
        
        if (departure != null && destination != null) {
            return rideRepository.findByDepartureContainingIgnoreCaseAndDestinationContainingIgnoreCase(
                    departure, destination);
        } else if (departure != null) {
            return rideRepository.findByDepartureContainingIgnoreCase(departure);
        } else if (destination != null) {
            return rideRepository.findByDestinationContainingIgnoreCase(destination);
        } else {
            return rideRepository.findAll();
        }
    }

    @Transactional
    public void updateAvailableSeats(Long rideId, Integer numberOfSeats) {
        log.info("Updating available seats for ride: {}", rideId);
        
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found with id: " + rideId));

        int newAvailableSeats = ride.getAvailableSeats() - numberOfSeats;
        
        if (newAvailableSeats < 0) {
            throw new IllegalArgumentException("Not enough available seats");
        }

        ride.setAvailableSeats(newAvailableSeats);
        rideRepository.save(ride);
        
        log.info("Updated available seats for ride {}: {} -> {}", 
                rideId, ride.getAvailableSeats() + numberOfSeats, newAvailableSeats);
    }

    /**
     * ✅ NOUVEAU : Supprimer un ride
     */
    @Transactional
    public void deleteRide(Long id) {
        log.info("Attempting to delete ride with id: {}", id);
        
        // Vérifier si le ride existe
        if (!rideRepository.existsById(id)) {
            throw new IllegalArgumentException("Ride not found with id: " + id);
        }
        
        // TODO: Ajouter une vérification pour les bookings actifs
        // Dans un système réel, on pourrait :
        // 1. Empêcher la suppression si des bookings existent
        // 2. Ou annuler tous les bookings automatiquement
        
        rideRepository.deleteById(id);
        log.info("Ride deleted successfully: {}", id);
    }
}