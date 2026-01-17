package payment.example.demo.repository;

import payment.example.demo.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByRideId(Long rideId);
    List<Payment> findByPassengerId(String passengerId);
    List<Payment> findByStatus(Payment.PaymentStatus status);
}

