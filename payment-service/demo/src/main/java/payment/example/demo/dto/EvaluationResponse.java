package payment.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.example.demo.model.Evaluation;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private Long id;
    private Long rideId;
    private Long bookingId;
    private String evaluatorId;
    private String evaluatedId;
    private Evaluation.EvaluationType type;
    private Integer rating;
    private String comment;
    private LocalDateTime evaluationTime;
}

