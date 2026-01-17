package booking.example.demo.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:booking.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.booking.key:booking.created}")
    private String bookingRoutingKey;

    public BookingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBookingCreated(Long bookingId, Long rideId, Integer numberOfSeats) {
        try {
            BookingEvent event = new BookingEvent(bookingId, rideId, numberOfSeats, "CREATED");
            rabbitTemplate.convertAndSend(exchangeName, bookingRoutingKey, event);
            log.info("Published booking created event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing booking created event", e);
        }
    }
}

