package payment.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment.example.demo.model.Payment;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private Long rideId;
    private String passengerId;
    private String driverId;
    private Double totalAmount;
    private Double sharedAmount;
    private Integer numberOfPassengers;
    private Payment.PaymentStatus status;
    private LocalDateTime paymentTime;
}

