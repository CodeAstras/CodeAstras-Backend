# CodeAstras Backend

This repository contains the backend server for CodeAstras, a collaborative real-time IDE and project management platform. It is built using Java and the Spring Boot framework.

## Project Overview

CodeAstras Backend provides the RESTful APIs and WebSocket endpoints necessary for the operation of the CodeAstras platform. It handles user authentication, project management, real-time collaboration via CRDT-like mechanisms (or operational transformation), chat functionality, and voice/video signaling.

## Technologies Used

- **Language:** Java 17+
- **Framework:** Spring Boot 3+
- **Build Tool:** Maven
- **Database:** PostgreSQL (or H2 for development)
- **Real-time Communication:** WebSocket (STOMP), WebRTC Signaling
- **Security:** Spring Security, JWT (JSON Web Tokens)

## Getting Started

Follow these instructions to set up and run the backend server locally.

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.8+
- PostgreSQL (optional, if using external DB)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/CodeAstras/CodeAstras-Backend.git
   ```

2. Navigate to the project directory:
   ```bash
   cd CodeAstras-Backend/backend
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

### Configuration

The application configuration can be found in `src/main/resources/application.properties`. You may need to update database credentials or server port configurations specific to your environment.

### Running the Application

You can run the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

Alternatively, you can run the generated JAR file from the `target` directory:

```bash
java -jar target/codeastras-0.0.1-SNAPSHOT.jar
```

The server will start on port `8080` by default.

## API Documentation

- **Base URL:** `http://localhost:8080/api`
- **Authentication:** Most endpoints require a valid JWT token in the `Authorization` header (`Bearer <token>`).

## Features

- **User Authentication:** Registration and login using JWT.
- **Workspace Management:** Create, read, update, and delete projects.
- **File System:** Virtual file system management for code projects.
- **Real-time Collaboration:** WebSocket handlers for code synchronization.
- **Communication:** Project-based chat and signaling for voice/video calls.
- **Presence:** Tracking online users and active collaborators in real-time.

## Contributing

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Commit your changes with descriptive messages.
4. Push to your branch and submit a Pull Request.

## License

All rights reserved.

*Built by CodeAstras Team*

