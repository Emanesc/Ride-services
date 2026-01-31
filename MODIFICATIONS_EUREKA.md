# Modifications avec Eureka et Nouveaux Services

## ✅ Modifications Effectuées

### 1. **Eureka Server** (Nouveau Service)
- **Port:** 8761
- **Fichier:** `eureka-server/`
- **Fonction:** Service de découverte pour tous les microservices

### 2. **Availability Service** (Nouveau Service)
- **Port:** 8084
- **Fichier:** `availability-service/`
- **Fonction:** Gère la disponibilité des trajets
- **Endpoint:** `GET /api/availability/ride/{rideId}`
- **Fonctionnalité:** Affiche un message si le service n'est pas disponible (tous les sièges réservés)

### 3. **Modifications des Modèles**

#### Ride Model
- Ajout de méthodes sans getters:
  - `isServiceAvailable()`: Vérifie si le service est disponible
  - `getAvailabilityMessage()`: Retourne le message de disponibilité

### 4. **Booking Service - Simplification**
- **Nouveau endpoint:** `POST /api/bookings/process/{bookingId}`
- **Fonctionnalité:** Traite un booking avec seulement l'ID (pas besoin d'envoyer rideId, prix, etc. dans le JSON)
- Les informations sont récupérées automatiquement depuis la base de données

### 5. **Payment Service - Connexion avec Autres Services**

#### Nouveaux Clients
- `BookingServiceClient`: Récupère les informations de booking via Eureka
- `RideServiceClient`: Récupère les informations de ride via Eureka

#### Nouveaux Endpoints
- `POST /api/payments/from-booking`: Crée un paiement avec seulement le booking ID
  ```json
  {
    "bookingId": 1,
    "driverId": "driver123"
  }
  ```

- `GET /api/payments/booking/{bookingId}/passenger-amount`: Calcule le montant spécifique pour un passager
  - Retourne: montant par passager, montant total pour ce passager, etc.

### 6. **Eureka Client Configuration**
Tous les services sont maintenant des clients Eureka:
- ✅ ride-service (port 8081)
- ✅ booking-service (port 8082)
- ✅ payment-service (port 8083)
- ✅ availability-service (port 8084)

## 🚀 Commandes pour Démarrer

### Ordre de démarrage recommandé:

```bash
# 1. Démarrer Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Démarrer Ride Service
cd ../ride-service/demo
mvn spring-boot:run

# 3. Démarrer Booking Service
cd ../../booking-service/demo
mvn spring-boot:run

# 4. Démarrer Payment Service
cd ../../payment-service/demo
mvn spring-boot:run

# 5. Démarrer Availability Service (optionnel)
cd ../../availability-service
mvn spring-boot:run
```

## 📋 Nouveaux Endpoints Postman

### Availability Service
```
GET http://localhost:8084/api/availability/ride/1
```

### Booking Service - Nouveau
```
POST http://localhost:8082/api/bookings/process/1
```
Body: Aucun (utilise seulement l'ID dans l'URL)

### Payment Service - Nouveaux
```
POST http://localhost:8083/api/payments/from-booking
Body:
{
  "bookingId": 1,
  "driverId": "driver123"
}
```

```
GET http://localhost:8083/api/payments/booking/1/passenger-amount
```

## 🔄 Flux de Communication

1. **Création d'un trajet** → Ride Service
2. **Vérification disponibilité** → Availability Service (via Eureka) → Ride Service
3. **Création booking** → Booking Service (vérifie via Ride Service via Eureka)
4. **Traitement booking par ID** → Booking Service récupère toutes les infos depuis la DB
5. **Création paiement** → Payment Service récupère booking et ride via Eureka
6. **Calcul montant passager** → Payment Service calcule à partir du booking ID

## 📝 Notes Importantes

1. **Eureka doit être démarré en premier** pour que les autres services puissent s'enregistrer
2. **Les services communiquent via les noms** (ride-service, booking-service, etc.) et non plus via les URLs directes
3. **LoadBalancer** est utilisé pour équilibrer la charge entre instances
4. **Les méthodes sans getters** dans le modèle Ride permettent de vérifier la disponibilité sans exposer directement les champs

