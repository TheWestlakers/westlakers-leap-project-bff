# westlakers-leap-project-bff
## Build Instructions:
### Initial Setup
./mvnw clean package
### Build Docker image
docker build -t leap-bff .
### Run Container
docker run -p 8081:8081 leap-bff
### Troubleshooting
docker images 

Run with the command: ./mvnw spring-boot:run
