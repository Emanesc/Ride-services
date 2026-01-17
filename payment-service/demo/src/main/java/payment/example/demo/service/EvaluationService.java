package payment.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment.example.demo.dto.EvaluationRequest;
import payment.example.demo.dto.EvaluationResponse;
import payment.example.demo.model.Evaluation;
import payment.example.demo.repository.EvaluationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    public EvaluationService(EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public EvaluationResponse createEvaluation(EvaluationRequest request) {
        log.info("Creating evaluation for ride {}: {} rating {} by {}", 
                request.getRideId(), request.getType(), request.getRating(), request.getEvaluatorId());

        Evaluation evaluation = new Evaluation();
        evaluation.setRideId(request.getRideId());
        evaluation.setBookingId(request.getBookingId());
        evaluation.setEvaluatorId(request.getEvaluatorId());
        evaluation.setEvaluatedId(request.getEvaluatedId());
        evaluation.setType(request.getType());
        evaluation.setRating(request.getRating());
        evaluation.setComment(request.getComment());
        evaluation.setEvaluationTime(LocalDateTime.now());

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        
        log.info("Evaluation created successfully with id: {}", savedEvaluation.getId());
        
        return mapToResponse(savedEvaluation);
    }

    public Optional<EvaluationResponse> getEvaluationById(Long id) {
        return evaluationRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<EvaluationResponse> getEvaluationsByRideId(Long rideId) {
        return evaluationRepository.findByRideId(rideId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EvaluationResponse> getEvaluationsByEvaluatedId(String evaluatedId) {
        return evaluationRepository.findByEvaluatedId(evaluatedId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Double getAverageRating(String evaluatedId) {
        List<Evaluation> evaluations = evaluationRepository.findByEvaluatedId(evaluatedId);
        if (evaluations.isEmpty()) {
            return 0.0;
        }
        double sum = evaluations.stream()
                .mapToInt(Evaluation::getRating)
                .sum();
        return Math.round((sum / evaluations.size()) * 10.0) / 10.0;
    }

    private EvaluationResponse mapToResponse(Evaluation evaluation) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .rideId(evaluation.getRideId())
                .bookingId(evaluation.getBookingId())
                .evaluatorId(evaluation.getEvaluatorId())
                .evaluatedId(evaluation.getEvaluatedId())
                .type(evaluation.getType())
                .rating(evaluation.getRating())
                .comment(evaluation.getComment())
                .evaluationTime(evaluation.getEvaluationTime())
                .build();
    }
}

