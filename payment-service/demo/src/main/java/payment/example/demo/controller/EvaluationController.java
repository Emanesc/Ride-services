package payment.example.demo.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.example.demo.dto.EvaluationRequest;
import payment.example.demo.dto.EvaluationResponse;
import payment.example.demo.service.EvaluationService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ResponseEntity<EvaluationResponse> createEvaluation(@Valid @RequestBody EvaluationRequest request) {
        try {
            log.info("Received evaluation request: {}", request);
            EvaluationResponse response = evaluationService.createEvaluation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid evaluation request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating evaluation", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationResponse> getEvaluationById(@PathVariable Long id) {
        try {
            log.info("Fetching evaluation with id: {}", id);
            return evaluationService.getEvaluationById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("Evaluation not found with id: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error fetching evaluation with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByRideId(@PathVariable Long rideId) {
        try {
            log.info("Fetching evaluations for ride: {}", rideId);
            List<EvaluationResponse> evaluations = evaluationService.getEvaluationsByRideId(rideId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error fetching evaluations for ride: {}", rideId, e);
            throw e;
        }
    }

    @GetMapping("/user/{evaluatedId}")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByEvaluatedId(@PathVariable String evaluatedId) {
        try {
            log.info("Fetching evaluations for user: {}", evaluatedId);
            List<EvaluationResponse> evaluations = evaluationService.getEvaluationsByEvaluatedId(evaluatedId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error fetching evaluations for user: {}", evaluatedId, e);
            throw e;
        }
    }

    @GetMapping("/user/{evaluatedId}/rating")
    public ResponseEntity<Map<String, Double>> getAverageRating(@PathVariable String evaluatedId) {
        try {
            log.info("Fetching average rating for user: {}", evaluatedId);
            Double averageRating = evaluationService.getAverageRating(evaluatedId);
            return ResponseEntity.ok(Map.of("averageRating", averageRating));
        } catch (Exception e) {
            log.error("Error fetching average rating for user: {}", evaluatedId, e);
            throw e;
        }
    }
}

