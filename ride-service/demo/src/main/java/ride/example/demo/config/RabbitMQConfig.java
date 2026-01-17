package ride.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name:booking.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.queue.booking.name:booking.queue}")
    private String bookingQueueName;

    @Value("${rabbitmq.routing.booking.key:booking.created}")
    private String bookingRoutingKey;

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue bookingQueue() {
        return QueueBuilder.durable(bookingQueueName).build();
    }

    @Bean
    public Binding bookingBinding() {
        return BindingBuilder
                .bind(bookingQueue())
                .to(bookingExchange())
                .with(bookingRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

