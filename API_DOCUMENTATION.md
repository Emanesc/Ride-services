# Documentation API - Système de Covoiturage

## 🚀 Commandes pour démarrer les services

### Prérequis
- MySQL doit être démarré avec les bases de données créées : `ride`, `booking`, `payment`
- RabbitMQ doit être démarré (port 5672)

### Démarrer les services

#### 1. Ride-Service (Port 8081)
```bash
cd ride-service/demo
mvn spring-boot:run
```

#### 2. Booking-Service (Port 8082)
```bash
cd booking-service/demo
mvn spring-boot:run
```

#### 3. Payment-Service (Port 8083)
```bash
cd payment-service/demo
mvn spring-boot:run
```

---

## 📋 Tests avec Postman

### RIDE-SERVICE (http://localhost:8081)

#### 1. Créer un trajet
**POST** `http://localhost:8081/api/rides`

**Body (JSON):**
```json
{
  "departure": "Paris",
  "destination": "Lyon",
  "departureTime": "2026-01-20T14:12:00",
  "availableSeats": 4,
  "price": 50.00
}
```

#### 2. Obtenir tous les trajets
**GET** `http://localhost:8081/api/rides`

#### 3. Obtenir un trajet par ID
**GET** `http://localhost:8081/api/rides/1`

#### 4. Vérifier la disponibilité d'un trajet
**GET** `http://localhost:8081/api/rides/1/availability`

#### 5. Rechercher des trajets
**GET** `http://localhost:8081/api/rides/search?departure=Paris&destination=Lyon`

---

### BOOKING-SERVICE (http://localhost:8082)

#### 1. Créer une réservation
**POST** `http://localhost:8082/api/bookings`

**Body (JSON):**
```json
{
  "rideId": 1,
  "passengerId": "passenger123",
  "numberOfSeats": 2
}
```

#### 2. Obtenir une réservation par ID
**GET** `http://localhost:8082/api/bookings/1`

#### 3. Obtenir toutes les réservations d'un trajet
**GET** `http://localhost:8082/api/bookings/ride/1`

#### 4. Obtenir toutes les réservations d'un passager
**GET** `http://localhost:8082/api/bookings/passenger/passenger123`

#### 5. Annuler une réservation
**PUT** `http://localhost:8082/api/bookings/1/cancel`

---

### PAYMENT-SERVICE (http://localhost:8083)

#### 1. Créer un paiement (calcul du montant partagé)
**POST** `http://localhost:8083/api/payments`

**Body (JSON):**
```json
{
  "bookingId": 1,
  "rideId": 1,
  "passengerId": "passenger123",
  "driverId": "driver456",
  "totalAmount": 50.00,
  "numberOfPassengers": 3
}
```

#### 2. Obtenir un paiement par ID
**GET** `http://localhost:8083/api/payments/1`

#### 3. Obtenir les paiements d'une réservation
**GET** `http://localhost:8083/api/payments/booking/1`

#### 4. Obtenir les paiements d'un trajet
**GET** `http://localhost:8083/api/payments/ride/1`

#### 5. Obtenir les paiements d'un passager
**GET** `http://localhost:8083/api/payments/passenger/passenger123`

---

### Évaluations (Payment-Service)

#### 1. Créer une évaluation
**POST** `http://localhost:8083/api/evaluations`

**Body (JSON) - Évaluer un conducteur:**
```json
{
  "rideId": 1,
  "bookingId": 1,
  "evaluatorId": "passenger123",
  "evaluatedId": "driver456",
  "type": "DRIVER",
  "rating": 5,
  "comment": "Excellent conducteur, très ponctuel!"
}
```

**Body (JSON) - Évaluer un passager:**
```json
{
  "rideId": 1,
  "bookingId": 1,
  "evaluatorId": "driver456",
  "evaluatedId": "passenger123",
  "type": "PASSENGER",
  "rating": 4,
  "comment": "Passager agréable et respectueux"
}
```

#### 2. Obtenir une évaluation par ID
**GET** `http://localhost:8083/api/evaluations/1`

#### 3. Obtenir les évaluations d'un trajet
**GET** `http://localhost:8083/api/evaluations/ride/1`

#### 4. Obtenir les évaluations d'un utilisateur
**GET** `http://localhost:8083/api/evaluations/user/driver456`

#### 5. Obtenir la note moyenne d'un utilisateur
**GET** `http://localhost:8083/api/evaluations/user/driver456/rating`

---

## 🔄 Scénario complet de test

### Étape 1: Créer un trajet
```http
POST http://localhost:8081/api/rides
Content-Type: application/json

{
  "departure": "Paris",
  "destination": "Lyon",
  "departureTime": "2024-12-25T10:00:00",
  "availableSeats": 4,
  "price": 50.00
}
```
**Résultat:** Notez l'`id` du trajet créé (ex: `1`)

### Étape 2: Vérifier la disponibilité
```http
GET http://localhost:8081/api/rides/1/availability
```
**Résultat:** Devrait retourner `available: true, availableSeats: 4`

### Étape 3: Créer une réservation
```http
POST http://localhost:8082/api/bookings
Content-Type: application/json

{
  "rideId": 1,
  "passengerId": "passenger123",
  "numberOfSeats": 2
}
```
**Résultat:** Réservation créée, notez l'`id` (ex: `1`)

### Étape 4: Vérifier que les sièges ont été mis à jour (via RabbitMQ)
```http
GET http://localhost:8081/api/rides/1/availability
```
**Résultat:** Devrait maintenant retourner `availableSeats: 2` (4 - 2 = 2)

### Étape 5: Créer un paiement
```http
POST http://localhost:8083/api/payments
Content-Type: application/json

{
  "bookingId": 1,
  "rideId": 1,
  "passengerId": "passenger123",
  "driverId": "driver456",
  "totalAmount": 50.00,
  "numberOfPassengers": 3
}
```
**Résultat:** Paiement créé avec `sharedAmount: 16.67` (50.00 / 3)

### Étape 6: Créer une évaluation
```http
POST http://localhost:8083/api/evaluations
Content-Type: application/json

{
  "rideId": 1,
  "bookingId": 1,
  "evaluatorId": "passenger123",
  "evaluatedId": "driver456",
  "type": "DRIVER",
  "rating": 5,
  "comment": "Excellent voyage!"
}
```

---

## 📝 Notes importantes

1. **Ordre de démarrage recommandé:**
   - MySQL (base de données)
   - RabbitMQ (messagerie)
   - Ride-Service
   - Booking-Service
   - Payment-Service

2. **Format de date:** Utilisez le format ISO 8601: `YYYY-MM-DDTHH:MM:SS`
   - Exemple: `2024-12-25T10:00:00`

3. **Headers Postman:**
   - Pour les requêtes POST/PUT, ajoutez: `Content-Type: application/json`

4. **Validation:** Les requêtes sont validées. En cas d'erreur, un message JSON sera retourné avec les détails.

