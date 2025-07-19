# Vehicle Manager API

The Vehicle Manager API is a RESTful Spring Boot application designed to manage vehicle-related operations for businesses or dealerships. It provides a robust backend with secure token-based authentication and supports full CRUD operations across multiple entities:

- Vehicles: Register and manage details about vehicles in inventory.

- Clients: Store and retrieve client information for sales and maintenance history.

- Finances: Track financial records, transactions, and related data.

- Maintenances: Log and manage maintenance records for vehicles.

- Sales: Record vehicle sales, associate them with clients, and manage sales history.

The API implements JWT (JSON Web Token) authentication to ensure secure access to all endpoints, enforcing user authentication and authorization for sensitive operations.

This API is ideal for integration with frontend dashboards, mobile apps, or dealer management systems.

## Config

To use this project you must create a new profile in resources with the name "application-dev.properties" for developer use with the parameters below

```
security.jwt.secret=<YOUR SECRET KEY>
spring.datasource.username=<DATABASE USERNAME>
spring.datasource.password=<DATABASE PASSWORD>
```
