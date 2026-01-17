package payment.example.demo.repository;

import payment.example.demo.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByRideId(Long rideId);
    List<Evaluation> findByEvaluatedId(String evaluatedId);
    List<Evaluation> findByType(Evaluation.EvaluationType type);
}

