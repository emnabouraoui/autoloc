# AutoLoc API — Postman tests

This folder contains an importable Postman collection and environment for the AutoLoc Spring Boot API.

## Prerequisites

- Java 17+ and Maven
- MySQL running on `localhost:3306`
- The API running on `http://localhost:8080`

Start the API from the project root:

```bash
./mvnw spring-boot:run
```

or:

```bash
./mvnw clean package
java -jar target/autoloc-api-0.0.1-SNAPSHOT.jar
```

## Import into Postman

1. Open Postman → **Import**.
2. Import:
   - `AutoLoc.postman_collection.json`
   - `AutoLoc.postman_environment.json`
3. Select the **AutoLoc Local** environment.
4. Run the collection in order (Collection Runner → **Run**).

The collection starts by creating its own test data, runs the negative tests, then deletes everything it created. It can be run repeatedly; unique values use Postman dynamic variables (`{{$timestamp}}`, `{{$randomInt}}`).

## Run from the command line

With the API running:

```bash
npx newman run postman/AutoLoc.postman_collection.json \
  -e postman/AutoLoc.postman_environment.json
```

## Coverage

| Folder | Requests |
|---|---|
| 01 - Agences | Create, get, update, list |
| 02 - Equipements | Create, get, update, list |
| 03 - Vehicules | Create with agence + équipement, get, update, list |
| 04 - Clients | Create, get, update, list |
| 05 - Employes | Create, get, update, list |
| 06 - Reservations | Create, get, confirm, list |
| 07 - Contrats | Create, get, update, list |
| 08 - Paiements | Create, get, update, list |
| 09 - Maintenances | Create, get, update, list |
| 10 - Negative tests | 404, unknown endpoint, unsupported HTTP method, Bean Validation, invalid enum, duplicate email/immatriculation, overlapping reservation, duplicate contract, invalid dates |
| 11 - Cleanup | Deletes in dependency order and verifies deletion |

## API endpoints

| Resource | Base path |
|---|---|
| Agences | `/api/agences` |
| Equipements | `/api/equipements` |
| Vehicules | `/api/vehicules` |
| Clients | `/api/clients` |
| Employes | `/api/employes` |
| Reservations | `/api/reservations` |
| Contrats | `/api/contrats` |
| Paiements | `/api/paiements` |
| Maintenances | `/api/maintenances` |

Each resource exposes:

- `GET /api/{resource}` — list
- `GET /api/{resource}/{id}` — detail
- `POST /api/{resource}` — create (`201`)
- `PUT /api/{resource}/{id}` — update (`200`)
- `DELETE /api/{resource}/{id}` — delete (`204`)

## Notes

- The collection stores created IDs in collection variables (`agenceId`, `vehiculeId`, etc.).
- Cleanup requests skip themselves automatically if their ID was never created (for example after a failed create).
- Errors use a consistent JSON shape: `timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`.
- If the API runs on another host/port, update `baseUrl` in the environment.
