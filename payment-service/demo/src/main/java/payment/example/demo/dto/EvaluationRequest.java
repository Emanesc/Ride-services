package payment.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import payment.example.demo.model.Evaluation;

@Data
public class EvaluationRequest {
    
    @NotNull(message = "Ride ID is required")
    private Long rideId;
    
    private Long bookingId;
    
    @NotBlank(message = "Evaluator ID is required")
    private String evaluatorId;
    
    @NotBlank(message = "Evaluated ID is required")
    private String evaluatedId;
    
    @NotNull(message = "Evaluation type is required")
    private Evaluation.EvaluationType type;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    private String comment;
}

