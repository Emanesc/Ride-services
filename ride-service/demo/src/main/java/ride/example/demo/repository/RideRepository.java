package ride.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ride.example.demo.model.Ride;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

}
