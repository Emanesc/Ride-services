package ride.example.demo.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ride.example.demo.model.Ride;
import ride.example.demo.repository.RideRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RideService {
     @Autowired
    private  RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public Ride createRide(Ride ride) {
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
}
