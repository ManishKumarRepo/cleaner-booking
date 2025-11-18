# Overview

The Cleaning Booking Application allows customers to book cleaning services with professional cleaners.
Key features include:

- Vehicle management and cleaner assignment

- Booking management with overlap validation

- Availability checking for cleaners

- Automatic handling of working hours and constraints


## Technologies

- Java 21

- Spring Boot 3.x

- Spring Data JPA (Hibernate)

- H2 / MySQL (depending on environment)

- Docker & Docker Compose

- MockMvc / JUnit 5 for testing

# Getting Started
1. Clone the repository:
> git clone https://github.com/ManishKumarRepo/cleaner-booking.git    
cd cleaning-booking

2. Build the application:
> ./mvnw clean package

3. Run the application via docker compose: build and start the application 
> docker-compose up --build


The following services will be available:

- cleaning-app – Spring Boot application (port 8080)

- db – MySQL database (port 3306)

#### sample docker-compose.yml file
```sh

version: "3.9"
services:
  db:
    image: mysql:8.1
    container_name: cleaning-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: cleaning_booking
      MYSQL_USER: user
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql

  app:
    build: .
    container_name: cleaning-app
    depends_on:
      - db
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/cleaning_booking
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_JPA_HIBERNATE_DDL_AUTO: update

volumes:
  db_data:
```
#### Swagger URL
> http://localhost:8080/swagger-ui/index.html

## REST API Endpoints
#### Vehicle Management

| Method | Endpoint                      | Description                        |
| ------ | ----------------------------- | ---------------------------------- |
| POST   | `/api/vehicles`               | Create a new vehicle               |
| POST   | `/api/vehicles/{id}/cleaners` | Add a cleaner to a vehicle         |
| GET    | `/api/vehicles`               | Get all vehicles with cleaners     |
| GET    | `/api/vehicles/{id}`          | Get a single vehicle with cleaners |

#### Booking Management
| Method | Endpoint             | Description                |
| ------ | -------------------- | -------------------------- |
| POST   | `/api/bookings`      | Create a new booking       |
| PUT    | `/api/bookings/{id}` | Update an existing booking |


#### Availability
| Method | Endpoint            | Description                                      |
| ------ | ------------------- | ------------------------------------------------ |
| POST   | `/api/availability` | Check availability for a date or a specific slot |

## Sample Requests & Responses

#### Create Vehicle

``` sh 

Request

POST /api/vehicles
Content-Type: application/json

{
 "name": "Van A"
}


Response

{
 "id": 1,
 "name": "Van A",
 "cleaners": []
}
``` 


#### Add Cleaner to Vehicle
```sh

Request

POST /api/vehicles/1/cleaners
Content-Type: application/json

{
 "name": "John Doe"
}


Response

{
 "id": 1,
 "name": "John Doe",
 "vehicleId": 1
}
```

#### Create Booking
```sh

Request

POST /api/bookings
Content-Type: application/json

{
"date": "2025-11-20",
"startTime": "10:00",
"durationMinutes": 120,
"cleanerCount": 2
}


Response

{
 "bookingId": 1,
 "date": "2025-11-20",
 "startTime": "10:00",
 "endTime": "12:00",
 "cleanerIds": [1, 2]
}
```

#### Check Availability (Date Only)
```sh 

Request

POST /api/availability
Content-Type: application/json

{
 "date": "2025-11-20"
}


Response

{
 "availableCleanerIds": [],
 "availableTimeSlots": [
  "08:00-10:00",
  "12:00-22:00"
 ]
}
```

#### Check Availability (Specific Slot)
```sh 

Request

POST /api/availability
Content-Type: application/json

{
 "date": "2025-11-20",
 "startTime": "10:00",
 "durationMinutes": 120
}


Response

{
 "availableCleanerIds": [1, 2],
 "availableTimeSlots": []
}
```

## Constraints

- Bookings cannot overlap within ±30 minutes.

- Booking duration must be 120 or 240 minutes.

- Working hours: 08:00–22:00, Monday–Thursday.

- Friday is a non-working day.
