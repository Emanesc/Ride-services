package ride.example.demo.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ride.example.demo.service.RideService;

@Slf4j
@Component
public class BookingEventListener {

    private final RideService rideService;

    public BookingEventListener(RideService rideService) {
        this.rideService = rideService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.booking.name:booking.queue}")
    public void handleBookingCreated(BookingEvent event) {
        try {
            log.info("Received booking event: {}", event);
            if ("CREATED".equals(event.getEventType())) {
                rideService.updateAvailableSeats(event.getRideId(), event.getNumberOfSeats());
                log.info("Updated available seats for ride {}: reserved {} seats", 
                        event.getRideId(), event.getNumberOfSeats());
            }
        } catch (Exception e) {
            log.error("Error processing booking event: {}", event, e);
        }
    }
}

