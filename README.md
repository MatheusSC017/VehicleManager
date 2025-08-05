# Vehicle Manager API

The Vehicle Manager API is a RESTful Spring Boot application designed to manage vehicle-related operations for businesses or dealerships. It provides a robust backend with secure token-based authentication and supports full CRUD operations across multiple entities.

## Features

- **Vehicle Management**: Register and manage details about vehicles in inventory.
- **Client Management**: Store and retrieve client information for sales and maintenance history.
- **Finance Tracking**: Track financial records, transactions, and related data.
- **Maintenance Logging**: Log and manage maintenance records for vehicles.
- **Sales Recording**: Record vehicle sales, associate them with clients, and manage sales history.
- **Secure Authentication**: Uses JSON Web Token (JWT) for secure, token-based authentication.

## Tech Stack

- **Java 17**: The application is built using Java version 17.
- **Spring Boot 3**: Core framework for building the application.
- **Spring Data JPA**: For data persistence and repository management.
- **PostgreSQL**: The primary database for the application.
- **H2 Database**: Used for running automated tests.
- **Maven**: Dependency management and build tool.
- **Spring Security**: For handling authentication and authorization.

## Prerequisites

Before you begin, ensure you have the following installed on your local machine:
- JDK 17
- Maven 3.x
- PostgreSQL

## Getting Started

Follow these steps to get a local copy of the project up and running.

### 1. Clone the Repository

```bash
git clone https://github.com/MatheusSC017/VehicleManager.git
cd VehicleManager
```

### 2. Database Configuration

The application uses PostgreSQL as its database. You need to have a PostgreSQL server running.

1.  Create a database, for example `vehicle_manager_db`.
2.  The application requires configuration properties to connect to the database. Create a file named `application-dev.properties` inside the `src/main/resources` directory.
3.  Add the following properties to the file, replacing the placeholder values with your actual database credentials:

```properties
# src/main/resources/application-dev.properties

# JWT Secret Key
security.jwt.secret=<YOUR_SECRET_KEY>

# PostgreSQL Datasource
spring.datasource.url=jdbc:postgresql://localhost:5432/vehicle_manager_db
spring.datasource.username=<DATABASE_USERNAME>
spring.datasource.password=<DATABASE_PASSWORD>
```

### 3. Build the Project

Use Maven to build the project and install dependencies:

```bash
mvn clean install
```

### 4. Run the Application

You can run the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

The application will start on the default port `8080`.

## Running the Tests

To run the automated tests, use the following Maven command:

```bash
mvn test
```

## API Endpoints

The API is structured around REST principles. All endpoints are prefixed with `/api`.

### Authentication

-   `POST /api/auth/login`: Authenticates a user and returns a JWT token.
-   `POST /api/auth/register`: Registers a new user.
-   `POST /api/auth/refresh`: Refresh user authentication and return a new token.

### Vehicles

-   `GET /api/vehicles/images`: Get a paginated list of vehicles with filtering options and one image url per record.
-   `GET /api/vehicles`: Get a paginated list of vehicles with filtering options.
-   `GET /api/vehicles/search`: Get a list of vehicles with more basic filtering options.
-   `GET /api/vehicles/chassi/{chassi}`: Get a specific vehicle by its Chassi.
-   `GET /api/vehicles/{id}`: Get a specific vehicle by its ID.
-   `POST /api/vehicles`: Create a new vehicle.
-   `PUT /api/vehicles/{id}`: Update an existing vehicle.
-   `DELETE /api/vehicles/{id}`: Delete a vehicle.

### Clients

-   `GET /api/clients`: Get a paginated list of clients.
-   `GET /api/clients/search`: Get a list of clients with filtering options.
-   `GET /api/clients/email/{email}`: Get a specific client by its email.
-   `GET /api/clients/{id}`: Get a specific client by its ID.
-   `POST /api/clients`: Create a new client.
-   `PUT /api/clients/{id}`: Update an existing client.
-   `DELETE /api/clients/{id}`: Delete a client.

### Sales

-   `GET /api/sales`: Get a paginated list of sales.
-   `GET /api/sales/vehicle/{id}`: Get a list of sales by its vehicle ID.
-   `GET /api/sales/{id}`: Get a specific sale by its ID.
-   `POST /api/sales`: Create a new sale.
-   `PUT /api/sales/{id}`: Update an existing sale.

### Maintenances

-   `GET /api/maintenances`: Get a paginated list of maintenances.
-   `GET /api/maintenances/vehicle/{id}`: Get a list of maintenances by its vehicle ID.
-   `GET /api/maintenances/{id}`: Get a specific maintenance by its ID.
-   `POST /api/maintenances`: Create a new maintenance.
-   `DELETE /api/maintenances/{id}`: Update an existing maintenance end date to today, marking the record as deleted.

### Financings

-   `GET /api/financings`: Get a paginated list of financings.
-   `GET /api/financings/{id}`: Get a specific financing by its ID.
-   `GET /api/financings/vehicle/{vehicleId}`: Get a specific financing by its vehicle ID and its status must be different from Canceled.
-   `POST /api/financings`: Create a new financing.
-   `PUT /api/financings/{id}`: Update an existing financing.
-   `PATCH /api/financings/{id}/status`: Update an existing financing status.

## Authentication

To access protected endpoints, you need to obtain a JWT token by calling the `/api/auth/login` endpoint with valid credentials. Include this token in the `Authorization` header of your requests as a Bearer token:

```
Authorization: Bearer <your-jwt-token>
```
