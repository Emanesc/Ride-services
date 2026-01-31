package payment.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "evaluations")
public class Evaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "ride_id")
    private Long rideId;
    
    @Column(name = "evaluator_id", length = 255)
    private String evaluatorId;
    
    @Column(name = "evaluated_id", length = 255)
    private String evaluatedId;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(length = 255)
    private String comment;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('DRIVER','PASSENGER')")
    private EvaluationType type;
    
    @Column(name = "evaluation_time")
    private LocalDateTime evaluationTime;
    
    @PrePersist
    protected void onCreate() {
        if (evaluationTime == null) {
            evaluationTime = LocalDateTime.now();
        }
    }
    
    public enum EvaluationType {
        DRIVER,
        PASSENGER
    }
}