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

There are currently 7 lines in the file, with 2 lines needing modification after the copy.

Also, copy the .env.example file into an .env:

```bat
copy .env.example .env
```

Then update `.env` with your local values. There are currently 7 lines in the file, with 4 lines needing modification after the copy.


# Remote Database SSH Tunnel Setup

## Quick Reference

**SSH Tunnel Command:**
```bash
ssh -N -L 5433:localhost:5432 <user>@<host ip>
```

**Key Configuration Changes:**
- `application-dev.properties`:
  - `spring.datasource.url=jdbc:postgresql://localhost:5433/westlakersdb` (port 5433, not 5432)
  - `spring.sql.init.mode=never` (skip script initialization for shared remote DB)
  - `spring.jpa.hibernate.ddl-auto=none`

## Verification
- Port listening: `netstat -tuln | grep 5433`
- Tunnel must stay open while developing
- Run `./mvnw spring-boot:run` in separate terminal

## Important Notes
- Remote DB already contains data—don't re-initialize
- Tunnel forwards local 5433 → remote localhost:5432
- Keep SSH tunnel terminal open and in foreground

## Run locally on Windows

```bat
mvn spring-boot:run
```

