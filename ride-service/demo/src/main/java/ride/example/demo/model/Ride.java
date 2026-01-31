package ride.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String departure;
    private String destination;
    private LocalDateTime departureTime;  // dateTime
    private int availableSeats;
    private double price;
    
    // ✅ NOUVEAUX CHAMPS - Correspondant à ta structure BDD
    @Column(name = "driver_id")
    private String driverId;
    
    @Column(name = "driver_name")
    private String driverName;
    
    @Column(name = "event_id")
    private String eventId;  // Si tu utilises ce champ dans ton système

    // Méthode pour vérifier la disponibilité sans utiliser de getter
    public boolean isServiceAvailable() {
        return availableSeats > 0;
    }

    // Méthode pour obtenir le message de disponibilité
    public String getAvailabilityMessage() {
        if (availableSeats <= 0) {
            return "Ce service n'est pas disponible - Tous les sièges sont réservés";
        }
        return "Service disponible - " + availableSeats + " siège(s) restant(s)";
    }
}