package payment.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rideId;
    private Long bookingId;
    private String evaluatorId; // ID of the person giving the evaluation
    private String evaluatedId; // ID of the person being evaluated (driver or passenger)
    
    @Enumerated(EnumType.STRING)
    private EvaluationType type; // DRIVER or PASSENGER
    
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating;
    
    private String comment;
    private LocalDateTime evaluationTime;
    
    public enum EvaluationType {
        DRIVER, PASSENGER
    }
}

