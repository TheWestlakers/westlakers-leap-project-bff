# westlakers-leap-project-bff

## Local development setup

The committed `src/main/resources/application-dev.properties` file no longer stores usernames or passwords.

Create a local override file from the example:

```bat
copy application-dev-local.example.properties application-dev-local.properties
```

Then update `application-dev-local.properties` with your local values, for example:

```properties
spring.datasource.username=postgres
spring.datasource.password=your-local-db-password
spring.security.user.name=devuser
spring.security.user.password=your-local-app-password
```

Required keys:

- `spring.datasource.username`
- `spring.datasource.password`
- `spring.security.user.name`
- `spring.security.user.password`

## Run locally on Windows

```bat
mvnw.cmd spring-boot:run
```
