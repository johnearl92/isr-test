# Test ISR
## Overview
This is a demo project for ISR for a simple project that returns a user in REST API service
## Solution

These are the following considerations I have come up with in scaling the database without sacrificing performance of the application:
- For the database, I'll be using a NoSQL database since they are better in handling a big set of data and Redis a key-value pair database will have the fastest searching if given the right key. as for the key , I am using user + loginTime under the assumption that the combination of the two will be unique. This will help in searching for user specific queries but it won't be fully use on this exercise
- I could also use SQL an implement indexing to better improve the handling of data
- For the demo will be using an in-memory data
- Sharding and partitioning can also help improve the performance of the database but this will be handle on the database setup
- For the service implementation will be using Spring Reactive frameworks for a nonblocking service - since data is quite big, this will help with the asynchronosity of the requests

- TODO:
- will try to create also an OpenApi for the testing
- rolling appender for logging 

-- Note--
- there were no instructions as to how the system will behave or what will be the default values

## Getting Started
### Prerequisites
- maven 3.5+
- Java 17 or later
- docker - deployment will be containerized-ready
- Redis

## Project Structure
The project follows a standard Maven directory structure:
```
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.test
│   │   │       └── ...
│   │   └── resources
│   │       └── application.properties
│   └── test
│       ├── java
│       │   └── com.example.test
│       │       └── ...
│       └── resources
│           └── ...
├── pom.xml
└── README.md
```



- The src/main/java directory contains the main Java source code of the project.
- The src/main/resources directory contains the application configuration files, such as application.properties.
- The src/test/java directory contains the test source code for the project.
- The src/test/resources directory contains the test-specific configuration files.

### Installation
1. Build the project

   ``mvn clean install``
2. start the development server

   ``mvn springboot:run``
   or
3. or start the application
   ``java -jar target/test-0.0.1-SNAPSHOT.jar`

This will start the server. There is also a docker compose included to start a Redis server which will be started once the application starts.

## Configuration
The application can be configured by modifying the `src/main/resources/application.properties` file. Make sure to update the following properties:

```yaml
# Redis Configuration
test:
   seed:
      size: 100000
spring:
   redis:
      host: localhost
      port: 6379
```

Adjust the values based on your Redis server configuration and desired seed data.

## Contributing
Contributions are welcome! For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
// TODO