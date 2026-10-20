# AutoLoc API

Spring Boot REST API for managing a car-rental agency: agencies, vehicles, equipment, clients, employees, reservations, contracts, payments, and maintenance.

The project is built as a layered Spring Boot application and comes with an importable Postman collection covering CRUD, validation, business rules, and cleanup.

## Tech stack

- Java 17
- Spring Boot 4.1.1
  - Spring Web
  - Spring Data JPA
  - Bean Validation
- MySQL 8
- Lombok
- Maven Wrapper
- Postman / Newman for API tests

## Features

- CRUD REST API for 9 resources
- Request/response DTOs with Bean Validation
- Layered architecture: `Controller → Service → Repository → Entity`
- Consistent JSON error responses for `400`, `404`, `405`, `409`, `415`, and `500`
- Reservation business rules:
  - End date cannot be before start date
  - No overlapping active reservations for the same vehicle
  - A vehicle under maintenance cannot be reserved
  - Cancelling/terminating a reservation remains possible while the vehicle is under maintenance
- JPA associations aligned with the case-study model
- Postman collection: 57 requests, 137 assertions, automatic cleanup

## Project structure

```text
src/main/java/tn/esprit/autoloc/
├── AutolocApiApplication.java
├── domain/                  # JPA entities and enums
├── repository/              # Spring Data JPA repositories
├── service/                 # Business logic
└── web/
    ├── controller/          # REST controllers
    ├── dto/                 # Request/response DTOs
    └── exception/           # Global error handling
```

## Getting started

### Prerequisites

- JDK 17+
- MySQL running on `localhost:3306`
- No Maven installation needed (use `./mvnw`)

### Database configuration

The default configuration is in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/autoloc_db_v2?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
```

The database `autoloc_db_v2` is created automatically if it does not exist. Update the username/password if your local MySQL setup is different.

### Run the application

```bash
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**.

### Build and run the JAR

```bash
./mvnw clean package
java -jar target/autoloc-api-0.0.1-SNAPSHOT.jar
```

## API endpoints

Base URL: `http://localhost:8080/api`

| Resource | Base path |
|---|---|
| Agencies | `/agences` |
| Equipment | `/equipements` |
| Vehicles | `/vehicules` |
| Clients | `/clients` |
| Employees | `/employes` |
| Reservations | `/reservations` |
| Contracts | `/contrats` |
| Payments | `/paiements` |
| Maintenance | `/maintenances` |

Each resource exposes:

| Method | Path | Description | Status |
|---|---|---|---|
| `GET` | `/api/{resource}` | List all | `200` |
| `GET` | `/api/{resource}/{id}` | Get by id | `200` |
| `POST` | `/api/{resource}` | Create | `201` |
| `PUT` | `/api/{resource}/{id}` | Update | `200` |
| `DELETE` | `/api/{resource}/{id}` | Delete | `204` |

### Example request

```bash
curl -X POST http://localhost:8080/api/agences \
  -H 'Content-Type: application/json' \
  -d '{
    "nom": "AutoLoc Tunis",
    "ville": "Tunis",
    "adresse": "12 rue de la République",
    "telephone": "+216 71 000 000"
  }'
```

Example response:

```json
{
  "idAgence": 1,
  "nom": "AutoLoc Tunis",
  "ville": "Tunis",
  "adresse": "12 rue de la République",
  "telephone": "+216 71 000 000"
}
```

### Error response format

```json
{
  "timestamp": "2026-10-20T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La validation des données a échoué",
  "path": "/api/vehicules",
  "fieldErrors": {
    "immatriculation": "must not be blank"
  }
}
```

## Data model

| Relationship | Type |
|---|---|
| `Agence → Vehicule` | One-to-many |
| `Agence → Employe` | One-to-many |
| `Vehicule ↔ Equipement` | Many-to-many |
| `Client → Reservation` | One-to-many |
| `Vehicule → Reservation` | One-to-many |
| `Vehicule → Maintenance` | One-to-many |
| `Reservation ↔ Contrat` | One-to-one |
| `Contrat → Paiement` | One-to-many |

## Postman tests

The `postman/` folder contains:

- `AutoLoc.postman_collection.json` — 57 requests, 137 assertions
- `AutoLoc.postman_environment.json` — `baseUrl = http://localhost:8080`
- `README.md` — detailed collection documentation

### Run with Postman

1. Start the API.
2. Import both JSON files into Postman.
3. Select the **AutoLoc Local** environment.
4. Open **AutoLoc API** → **Run collection**.
5. Click **Run AutoLoc API**.

### Run with Newman

```bash
npx newman run postman/AutoLoc.postman_collection.json \
  -e postman/AutoLoc.postman_environment.json
```

Expected result:

```text
requests:   57
assertions: 137
failures:   0
```

The collection creates its own data, runs the negative tests, and deletes everything in the cleanup folder. It can be run repeatedly.

## Verification

```bash
./mvnw clean package
```

The Maven build must pass before running the Postman collection.

## Notes

- `spring.jpa.hibernate.ddl-auto=update` is used for local development.
- The project does not include authentication/authorization.
- MySQL credentials in `application.properties` are local development defaults.
