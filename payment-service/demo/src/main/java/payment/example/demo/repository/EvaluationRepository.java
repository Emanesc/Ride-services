package payment.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import payment.example.demo.model.Evaluation;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    
    /**
     * Find all evaluations for a specific user (as evaluated)
     */
    List<Evaluation> findByEvaluatedId(String evaluatedId);
    
    /**
     * Find all evaluations made by a specific user (as evaluator)
     */
    List<Evaluation> findByEvaluatorId(String evaluatorId);
    
    /**
     * Find all evaluations for a specific booking
     */
    List<Evaluation> findByBookingId(Long bookingId);
    
    /**
     * Find all evaluations for a specific ride
     */
    List<Evaluation> findByRideId(Long rideId);
    
    /**
     * Find evaluations by type (DRIVER or PASSENGER)
     */
    List<Evaluation> findByType(Evaluation.EvaluationType type);
    
    /**
     * Find driver evaluations for a specific user
     */
    List<Evaluation> findByEvaluatedIdAndType(String evaluatedId, Evaluation.EvaluationType type);
    
    /**
     * Calculate average rating for a user
     */
    @Query("SELECT AVG(e.rating) FROM Evaluation e WHERE e.evaluatedId = :userId")
    Double getAverageRatingForUser(String userId);
    
    /**
     * Calculate average rating for a user by type
     */
    @Query("SELECT AVG(e.rating) FROM Evaluation e WHERE e.evaluatedId = :userId AND e.type = :type")
    Double getAverageRatingForUserByType(String userId, Evaluation.EvaluationType type);
    
    /**
     * Count evaluations for a user
     */
    Long countByEvaluatedId(String evaluatedId);
}