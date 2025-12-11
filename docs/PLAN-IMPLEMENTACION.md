# Plan Detallado de Implementación - Sistema Ticketero

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Tech Lead:** Amazon Q Developer

---

## 1. Introducción

### 1.1 Objetivo del Plan

Este documento proporciona un plan de implementación paso a paso para construir el Sistema Ticketero Digital completo. Un desarrollador mid-level puede seguir este plan sin consultar documentación adicional.

### 1.2 Tiempo Estimado

**Total:** 11 horas de implementación distribuidas en 3 días:
- **Día 1:** 4 horas (Setup + Entities + DTOs + Repositories)
- **Día 2:** 5 horas (Services + Controllers)
- **Día 3:** 2 horas (Schedulers + Testing)

### 1.3 Prerrequisitos

- Java 21 instalado
- Maven 3.9+
- Docker Desktop
- IDE con soporte Lombok
- Cuenta Telegram Bot (opcional para testing)

---

## 2. Estructura del Proyecto

```
ticketero/
├── pom.xml                                    # Maven configuration
├── .env                                       # Variables de entorno (gitignored)
├── docker-compose.yml                         # PostgreSQL + API
├── Dockerfile                                 # Multi-stage build
├── README.md                                  # Instrucciones del proyecto
│
├── src/
│   ├── main/
│   │   ├── java/com/example/ticketero/
│   │   │   │
│   │   │   ├── TicketeroApplication.java    # Main class con @EnableScheduling
│   │   │   │
│   │   │   ├── controller/                   # REST Controllers
│   │   │   │   ├── TicketController.java
│   │   │   │   └── AdminController.java
│   │   │   │
│   │   │   ├── service/                      # Business Logic
│   │   │   │   ├── TicketService.java
│   │   │   │   ├── TelegramService.java
│   │   │   │   ├── QueueManagementService.java
│   │   │   │   ├── AdvisorService.java
│   │   │   │   └── NotificationService.java
│   │   │   │
│   │   │   ├── repository/                   # Data Access
│   │   │   │   ├── TicketRepository.java
│   │   │   │   ├── MensajeRepository.java
│   │   │   │   └── AdvisorRepository.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── entity/                   # JPA Entities
│   │   │   │   │   ├── Ticket.java
│   │   │   │   │   ├── Mensaje.java
│   │   │   │   │   └── Advisor.java
│   │   │   │   │
│   │   │   │   ├── dto/                      # DTOs
│   │   │   │   │   ├── TicketCreateRequest.java
│   │   │   │   │   ├── TicketResponse.java
│   │   │   │   │   ├── QueuePositionResponse.java
│   │   │   │   │   ├── DashboardResponse.java
│   │   │   │   │   └── QueueStatusResponse.java
│   │   │   │   │
│   │   │   │   └── enums/                    # Enumerations
│   │   │   │       ├── QueueType.java
│   │   │   │       ├── TicketStatus.java
│   │   │   │       ├── AdvisorStatus.java
│   │   │   │       └── MessageTemplate.java
│   │   │   │
│   │   │   ├── scheduler/                    # Scheduled Tasks
│   │   │   │   ├── MensajeScheduler.java
│   │   │   │   └── QueueProcessorScheduler.java
│   │   │   │
│   │   │   ├── config/                       # Configuration
│   │   │   │   ├── RestTemplateConfig.java
│   │   │   │   └── TelegramConfig.java
│   │   │   │
│   │   │   └── exception/                    # Exception Handling
│   │   │       ├── TicketNotFoundException.java
│   │   │       ├── TicketActivoExistenteException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml               # Spring Boot config
│   │       ├── application-dev.yml           # Dev profile
│   │       ├── application-prod.yml          # Prod profile
│   │       │
│   │       └── db/migration/                 # Flyway migrations
│   │           ├── V1__create_ticket_table.sql
│   │           ├── V2__create_mensaje_table.sql
│   │           └── V3__create_advisor_table.sql
│   │
│   └── test/
│       └── java/com/example/ticketero/
│           ├── service/
│           │   ├── TicketServiceTest.java
│           │   └── TelegramServiceTest.java
│           │
│           └── controller/
│               └── TicketControllerTest.java
│
└── docs/                                      # Documentación
    ├── REQUERIMIENTOS-NEGOCIO.md
    ├── ARQUITECTURA.md
    └── diagrams/
        ├── 01-context-diagram.puml
        ├── 02-sequence-diagram.puml
        └── 03-er-diagram.puml
```

---

## 3. Configuración Inicial

### 3.1 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.11</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>ticketero</artifactId>
    <version>1.0.0</version>
    <name>Ticketero API</name>
    <description>Sistema de Gestión de Tickets con Notificaciones en Tiempo Real</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway for Database Migrations -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 application.yml

```yaml
spring:
  application:
    name: ticketero-api

  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/ticketero}
    username: ${DATABASE_USERNAME:dev}
    password: ${DATABASE_PASSWORD:dev123}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate  # Flyway maneja el schema
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration

# Telegram Configuration
telegram:
  bot-token: ${TELEGRAM_BOT_TOKEN}
  api-url: https://api.telegram.org/bot

# Actuator Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

# Logging
logging:
  level:
    com.example.ticketero: INFO
    org.springframework: WARN
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### 3.3 .env

```bash
# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here

# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/ticketero
DATABASE_USERNAME=dev
DATABASE_PASSWORD=dev123

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
```

### 3.4 docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: ticketero-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: ticketero
      POSTGRES_USER: dev
      POSTGRES_PASSWORD: dev123
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dev -d ticketero"]
      interval: 10s
      timeout: 5s
      retries: 5

  api:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ticketero-api
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/ticketero
      DATABASE_USERNAME: dev
      DATABASE_PASSWORD: dev123
      TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN}
      SPRING_PROFILES_ACTIVE: dev
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped

volumes:
  postgres_data:
    driver: local
```

### 3.5 Dockerfile

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 4. Migraciones de Base de Datos

### 4.1 V1__create_ticket_table.sql

```sql
-- V1__create_ticket_table.sql
-- Tabla principal de tickets

CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    codigo_referencia UUID NOT NULL UNIQUE,
    numero VARCHAR(10) NOT NULL UNIQUE,
    national_id VARCHAR(20) NOT NULL,
    telefono VARCHAR(20),
    branch_office VARCHAR(100) NOT NULL,
    queue_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_in_queue INTEGER NOT NULL,
    estimated_wait_minutes INTEGER NOT NULL,
    assigned_advisor_id BIGINT,
    assigned_module_number INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_national_id ON ticket(national_id);
CREATE INDEX idx_ticket_queue_type ON ticket(queue_type);
CREATE INDEX idx_ticket_created_at ON ticket(created_at DESC);

-- Comentarios para documentación
COMMENT ON TABLE ticket IS 'Tickets de atención en sucursales';
COMMENT ON COLUMN ticket.codigo_referencia IS 'UUID único para referencias externas';
COMMENT ON COLUMN ticket.numero IS 'Número visible del ticket (C01, P15, etc.)';
COMMENT ON COLUMN ticket.position_in_queue IS 'Posición actual en cola (calculada en tiempo real)';
COMMENT ON COLUMN ticket.estimated_wait_minutes IS 'Tiempo estimado de espera en minutos';
```

### 4.2 V2__create_mensaje_table.sql

```sql
-- V2__create_mensaje_table.sql
-- Tabla de mensajes programados para Telegram

CREATE TABLE mensaje (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    plantilla VARCHAR(50) NOT NULL,
    estado_envio VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_programada TIMESTAMP NOT NULL,
    fecha_envio TIMESTAMP,
    telegram_message_id VARCHAR(50),
    intentos INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_mensaje_ticket 
        FOREIGN KEY (ticket_id) 
        REFERENCES ticket(id) 
        ON DELETE CASCADE
);

-- Índices para performance del scheduler
CREATE INDEX idx_mensaje_estado_fecha ON mensaje(estado_envio, fecha_programada);
CREATE INDEX idx_mensaje_ticket_id ON mensaje(ticket_id);

-- Comentarios
COMMENT ON TABLE mensaje IS 'Mensajes programados para envío vía Telegram';
COMMENT ON COLUMN mensaje.plantilla IS 'Tipo de mensaje: totem_ticket_creado, totem_proximo_turno, totem_es_tu_turno';
COMMENT ON COLUMN mensaje.estado_envio IS 'Estado: PENDIENTE, ENVIADO, FALLIDO';
COMMENT ON COLUMN mensaje.intentos IS 'Cantidad de reintentos de envío';
```

### 4.3 V3__create_advisor_table.sql

```sql
-- V3__create_advisor_table.sql
-- Tabla de asesores/ejecutivos

CREATE TABLE advisor (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    module_number INTEGER NOT NULL,
    assigned_tickets_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_module_number CHECK (module_number BETWEEN 1 AND 5),
    CONSTRAINT chk_assigned_count CHECK (assigned_tickets_count >= 0)
);

-- Índice para búsqueda de asesores disponibles
CREATE INDEX idx_advisor_status ON advisor(status);
CREATE INDEX idx_advisor_module ON advisor(module_number);

-- Foreign key de ticket a advisor (se agrega ahora que advisor existe)
ALTER TABLE ticket
    ADD CONSTRAINT fk_ticket_advisor 
    FOREIGN KEY (assigned_advisor_id) 
    REFERENCES advisor(id) 
    ON DELETE SET NULL;

-- Datos iniciales: 5 asesores
INSERT INTO advisor (name, email, status, module_number) VALUES
    ('María González', 'maria.gonzalez@institucion.cl', 'AVAILABLE', 1),
    ('Juan Pérez', 'juan.perez@institucion.cl', 'AVAILABLE', 2),
    ('Ana Silva', 'ana.silva@institucion.cl', 'AVAILABLE', 3),
    ('Carlos Rojas', 'carlos.rojas@institucion.cl', 'AVAILABLE', 4),
    ('Patricia Díaz', 'patricia.diaz@institucion.cl', 'AVAILABLE', 5);

-- Comentarios
COMMENT ON TABLE advisor IS 'Asesores/ejecutivos que atienden clientes';
COMMENT ON COLUMN advisor.status IS 'Estado: AVAILABLE, BUSY, OFFLINE';
COMMENT ON COLUMN advisor.module_number IS 'Número de módulo de atención (1-5)';
COMMENT ON COLUMN advisor.assigned_tickets_count IS 'Cantidad de tickets actualmente asignados';
```

---

## 5. Implementación por Fases

### Fase 0: Setup del Proyecto (30 minutos)

**Objetivo:** Configurar el proyecto base y verificar que compila

**Tareas:**
- [ ] Crear proyecto Maven con estructura de carpetas
- [ ] Configurar pom.xml con todas las dependencias
- [ ] Crear application.yml con configuración base
- [ ] Crear .env con variables de entorno
- [ ] Crear docker-compose.yml para PostgreSQL
- [ ] Levantar base de datos: `docker-compose up -d postgres`
- [ ] Crear clase principal TicketeroApplication.java con @SpringBootApplication y @EnableScheduling
- [ ] Verificar compilación: `mvn clean compile`
- [ ] Verificar que conecta a BD: `mvn spring-boot:run`

**Criterios de Aceptación:**
- ✅ Proyecto compila sin errores
- ✅ Aplicación inicia y conecta a PostgreSQL
- ✅ Logs muestran: "Started TicketeroApplication"
- ✅ Actuator health endpoint responde: `curl http://localhost:8080/actuator/health`

**Ejemplo TicketeroApplication.java:**
```java
package com.example.ticketero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketeroApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketeroApplication.class, args);
    }
}
```

### Fase 1: Migraciones y Enumeraciones (45 minutos)

**Objetivo:** Crear esquema de base de datos y enumeraciones Java

**Tareas:**
- [ ] Crear V1__create_ticket_table.sql
- [ ] Crear V2__create_mensaje_table.sql
- [ ] Crear V3__create_advisor_table.sql
- [ ] Crear enum QueueType.java
- [ ] Crear enum TicketStatus.java
- [ ] Crear enum AdvisorStatus.java
- [ ] Crear enum MessageTemplate.java
- [ ] Reiniciar aplicación y verificar migraciones
- [ ] Verificar tablas creadas: `\dt` en psql
- [ ] Verificar datos iniciales: `SELECT * FROM advisor;`

**Criterios de Aceptación:**
- ✅ Flyway ejecuta las 3 migraciones exitosamente
- ✅ Tabla flyway_schema_history muestra 3 versiones
- ✅ Tablas ticket, mensaje, advisor existen
- ✅ 5 asesores iniciales insertados en advisor
- ✅ 4 enums creadas con valores correctos

**Ejemplo QueueType.java:**
```java
package com.example.ticketero.model.enums;

public enum QueueType {
    CAJA("Caja", 5, 1),
    PERSONAL_BANKER("Personal Banker", 15, 2),
    EMPRESAS("Empresas", 20, 3),
    GERENCIA("Gerencia", 30, 4);

    private final String displayName;
    private final int avgTimeMinutes;
    private final int priority;

    QueueType(String displayName, int avgTimeMinutes, int priority) {
        this.displayName = displayName;
        this.avgTimeMinutes = avgTimeMinutes;
        this.priority = priority;
    }

    public String getDisplayName() { return displayName; }
    public int getAvgTimeMinutes() { return avgTimeMinutes; }
    public int getPriority() { return priority; }
}
```

### Fase 2: Entities (1 hora)

**Objetivo:** Crear las 3 entidades JPA mapeadas a las tablas

**Tareas:**
- [ ] Crear Ticket.java con todas las anotaciones JPA
- [ ] Crear Mensaje.java con relación a Ticket
- [ ] Crear Advisor.java con relación a Ticket
- [ ] Usar Lombok: @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder
- [ ] Mapear enums con @Enumerated(EnumType.STRING)
- [ ] Configurar relaciones: @OneToMany, @ManyToOne
- [ ] Agregar @PrePersist para codigo_referencia UUID
- [ ] Compilar y verificar sin errores

**Criterios de Aceptación:**
- ✅ 3 entities creadas con anotaciones JPA correctas
- ✅ Relaciones bidireccionales configuradas
- ✅ Proyecto compila sin errores
- ✅ Hibernate valida el schema al iniciar (no crea tablas por ddl-auto=validate)

**Ejemplo Ticket.java:**
```java
package com.example.ticketero.model.entity;

import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_referencia", nullable = false, unique = true)
    private UUID codigoReferencia;

    @Column(name = "numero", nullable = false, unique = true, length = 10)
    private String numero;

    @Column(name = "national_id", nullable = false, length = 20)
    private String nationalId;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "branch_office", nullable = false, length = 100)
    private String branchOffice;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", nullable = false, length = 20)
    private QueueType queueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "position_in_queue", nullable = false)
    private Integer positionInQueue;

    @Column(name = "estimated_wait_minutes", nullable = false)
    private Integer estimatedWaitMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_advisor_id")
    @ToString.Exclude
    private Advisor assignedAdvisor;

    @Column(name = "assigned_module_number")
    private Integer assignedModuleNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<Mensaje> mensajes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        codigoReferencia = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Fase 3: DTOs (45 minutos)

**Objetivo:** Crear DTOs para request/response

**Tareas:**
- [ ] Crear TicketCreateRequest.java con Bean Validation
- [ ] Crear TicketResponse.java como record
- [ ] Crear QueuePositionResponse.java
- [ ] Crear DashboardResponse.java
- [ ] Crear QueueStatusResponse.java
- [ ] Agregar validaciones: @NotBlank, @NotNull, @Pattern
- [ ] Compilar y verificar

**Criterios de Aceptación:**
- ✅ 5 DTOs creados
- ✅ Validaciones Bean Validation configuradas
- ✅ Records usados donde sea apropiado (inmutabilidad)

**Ejemplo TicketCreateRequest.java:**
```java
package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TicketCreateRequest(
    
    @NotBlank(message = "El RUT/ID es obligatorio")
    String nationalId,
    
    @Pattern(regexp = "^\\+56[0-9]{9}$", message = "Teléfono debe tener formato +56XXXXXXXXX")
    String telefono,
    
    @NotBlank(message = "La sucursal es obligatoria")
    String branchOffice,
    
    @NotNull(message = "El tipo de cola es obligatorio")
    QueueType queueType
) {}
```

**Ejemplo TicketResponse.java:**
```java
package com.example.ticketero.model.dto;

import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
    Long id,
    UUID codigoReferencia,
    String numero,
    String nationalId,
    String telefono,
    String branchOffice,
    QueueType queueType,
    TicketStatus status,
    Integer positionInQueue,
    Integer estimatedWaitMinutes,
    String assignedAdvisorName,
    Integer assignedModuleNumber,
    LocalDateTime createdAt
) {}
```

### Fase 4: Repositories (30 minutos)

**Objetivo:** Crear interfaces de acceso a datos

**Tareas:**
- [ ] Crear TicketRepository.java extends JpaRepository
- [ ] Crear MensajeRepository.java
- [ ] Crear AdvisorRepository.java
- [ ] Agregar queries custom con @Query
- [ ] Métodos: findByCodigoReferencia, findByNationalIdAndStatusIn, etc.

**Criterios de Aceptación:**
- ✅ 3 repositories creados
- ✅ Queries custom documentadas
- ✅ Proyecto compila

**Ejemplo TicketRepository.java:**
```java
package com.example.ticketero.repository;

import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.QueueType;
import com.example.ticketero.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoReferencia(UUID codigoReferencia);

    Optional<Ticket> findByNumero(String numero);

    @Query("SELECT t FROM Ticket t WHERE t.nationalId = :nationalId AND t.status IN :statuses")
    Optional<Ticket> findByNationalIdAndStatusIn(
        @Param("nationalId") String nationalId, 
        @Param("statuses") List<TicketStatus> statuses
    );

    @Query("SELECT t FROM Ticket t WHERE t.status = :status ORDER BY t.createdAt ASC")
    List<Ticket> findByStatusOrderByCreatedAtAsc(@Param("status") TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.queueType = :queueType AND t.status = :status ORDER BY t.createdAt ASC")
    List<Ticket> findByQueueTypeAndStatusOrderByCreatedAtAsc(
        @Param("queueType") QueueType queueType,
        @Param("status") TicketStatus status
    );

    long countByQueueTypeAndStatus(QueueType queueType, TicketStatus status);
}
```

### Fase 5: Services (3 horas)

**Objetivo:** Implementar toda la lógica de negocio

**Orden de Implementación:**
1. TelegramService (sin dependencias)
2. AdvisorService (solo repository)
3. TicketService (usa TelegramService)
4. QueueManagementService (usa TicketService, AdvisorService)
5. NotificationService (usa TelegramService)

**Tareas:**
- [ ] Crear TelegramService.java (envío de mensajes)
- [ ] Crear TicketService.java (crear ticket, calcular posición)
- [ ] Crear QueueManagementService.java (asignación automática)
- [ ] Crear AdvisorService.java (gestión de asesores)
- [ ] Crear NotificationService.java (coordinar notificaciones)
- [ ] Implementar lógica según RN-001 a RN-013
- [ ] Agregar @Transactional donde corresponda
- [ ] Logging con @Slf4j

**Criterios de Aceptación:**
- ✅ 5 services implementados
- ✅ Reglas de negocio RN-001 a RN-013 aplicadas
- ✅ Transacciones configuradas correctamente
- ✅ Tests unitarios básicos pasan

**Ejemplo TicketService.java:**
```java
package com.example.ticketero.service;

import com.example.ticketero.exception.TicketActivoExistenteException;
import com.example.ticketero.model.dto.TicketCreateRequest;
import com.example.ticketero.model.dto.TicketResponse;
import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.model.entity.Ticket;
import com.example.ticketero.model.enums.MessageTemplate;
import com.example.ticketero.model.enums.TicketStatus;
import com.example.ticketero.repository.MensajeRepository;
import com.example.ticketero.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final MensajeRepository mensajeRepository;

    @Transactional
    public TicketResponse crearTicket(TicketCreateRequest request) {
        log.info("Creando ticket para nationalId: {}", request.nationalId());

        // RN-001: Validar ticket activo existente
        validarTicketActivoExistente(request.nationalId());

        // Generar número según RN-005, RN-006
        String numero = generarNumeroTicket(request.queueType());

        // Calcular posición según RN-010
        int posicion = calcularPosicionEnCola(request.queueType());
        int tiempoEstimado = calcularTiempoEstimado(posicion, request.queueType());

        // Crear y guardar ticket
        Ticket ticket = Ticket.builder()
            .nationalId(request.nationalId())
            .telefono(request.telefono())
            .branchOffice(request.branchOffice())
            .queueType(request.queueType())
            .status(TicketStatus.EN_ESPERA)
            .positionInQueue(posicion)
            .estimatedWaitMinutes(tiempoEstimado)
            .numero(numero)
            .build();

        ticket = ticketRepository.save(ticket);

        // Programar 3 mensajes (si hay teléfono)
        if (request.telefono() != null && !request.telefono().isBlank()) {
            programarMensajes(ticket);
        }

        log.info("Ticket creado: {}", ticket.getNumero());
        return toResponse(ticket);
    }

    public Optional<TicketResponse> findByCodigoReferencia(UUID codigoReferencia) {
        return ticketRepository.findByCodigoReferencia(codigoReferencia)
            .map(this::toResponse);
    }

    private void validarTicketActivoExistente(String nationalId) {
        List<TicketStatus> estadosActivos = List.of(
            TicketStatus.EN_ESPERA, 
            TicketStatus.PROXIMO, 
            TicketStatus.ATENDIENDO
        );
        
        ticketRepository.findByNationalIdAndStatusIn(nationalId, estadosActivos)
            .ifPresent(t -> {
                throw new TicketActivoExistenteException(
                    "Ya tienes un ticket activo: " + t.getNumero()
                );
            });
    }

    private String generarNumeroTicket(com.example.ticketero.model.enums.QueueType queueType) {
        // RN-005, RN-006: Generar número con prefijo
        String prefijo = switch (queueType) {
            case CAJA -> "C";
            case PERSONAL_BANKER -> "P";
            case EMPRESAS -> "E";
            case GERENCIA -> "G";
        };
        
        // Obtener siguiente número secuencial (simplificado)
        long count = ticketRepository.count() + 1;
        return prefijo + String.format("%02d", count % 100);
    }

    private int calcularPosicionEnCola(com.example.ticketero.model.enums.QueueType queueType) {
        // RN-010: Calcular posición basada en tickets EN_ESPERA
        return (int) ticketRepository.countByQueueTypeAndStatus(queueType, TicketStatus.EN_ESPERA) + 1;
    }

    private int calcularTiempoEstimado(int posicion, com.example.ticketero.model.enums.QueueType queueType) {
        // RN-010: tiempo = posición × tiempo promedio
        return posicion * queueType.getAvgTimeMinutes();
    }

    private void programarMensajes(Ticket ticket) {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Mensaje 1: Inmediato
        Mensaje mensaje1 = Mensaje.builder()
            .ticket(ticket)
            .plantilla(MessageTemplate.totem_ticket_creado)
            .estadoEnvio("PENDIENTE")
            .fechaProgramada(ahora)
            .build();
        
        // Mensaje 2: Cuando posición <= 3 (se programa dinámicamente)
        // Mensaje 3: Al asignar asesor (se programa dinámicamente)
        
        mensajeRepository.save(mensaje1);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getCodigoReferencia(),
            ticket.getNumero(),
            ticket.getNationalId(),
            ticket.getTelefono(),
            ticket.getBranchOffice(),
            ticket.getQueueType(),
            ticket.getStatus(),
            ticket.getPositionInQueue(),
            ticket.getEstimatedWaitMinutes(),
            ticket.getAssignedAdvisor() != null ? ticket.getAssignedAdvisor().getName() : null,
            ticket.getAssignedModuleNumber(),
            ticket.getCreatedAt()
        );
    }
}
```

### Fase 6: Controllers (2 horas)

**Objetivo:** Exponer API REST

**Tareas:**
- [ ] Crear TicketController.java (endpoints públicos)
- [ ] Crear AdminController.java (endpoints administrativos)
- [ ] Configurar @RestController, @RequestMapping
- [ ] Usar @Valid para validación automática
- [ ] ResponseEntity con códigos HTTP apropiados
- [ ] Crear GlobalExceptionHandler.java para errores

**Endpoints a Implementar:**

**TicketController:**
- POST /api/tickets - Crear ticket
- GET /api/tickets/{uuid} - Obtener ticket
- GET /api/tickets/{numero}/position - Consultar posición
- GET /api/health - Health check

**AdminController:**
- GET /api/admin/dashboard - Dashboard completo
- GET /api/admin/queues/{type} - Estado de cola
- GET /api/admin/queues/{type}/stats - Estadísticas
- GET /api/admin/advisors - Lista asesores
- GET /api/admin/advisors/stats - Estadísticas asesores
- PUT /api/admin/advisors/{id}/status - Cambiar estado
- GET /api/admin/summary - Resumen ejecutivo

**Criterios de Aceptación:**
- ✅ 11 endpoints implementados
- ✅ Validación automática funciona
- ✅ Manejo de errores centralizado
- ✅ Códigos HTTP correctos (200, 201, 400, 404, 409)

**Ejemplo TicketController.java:**
```java
package com.example.ticketero.controller;

import com.example.ticketero.model.dto.TicketCreateRequest;
import com.example.ticketero.model.dto.TicketResponse;
import com.example.ticketero.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> crearTicket(
        @Valid @RequestBody TicketCreateRequest request
    ) {
        log.info("POST /api/tickets - Creando ticket para {}", request.nationalId());
        
        TicketResponse response = ticketService.crearTicket(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/{codigoReferencia}")
    public ResponseEntity<TicketResponse> obtenerTicket(
        @PathVariable UUID codigoReferencia
    ) {
        log.info("GET /api/tickets/{}", codigoReferencia);
        
        return ticketService.findByCodigoReferencia(codigoReferencia)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
```

### Fase 7: Schedulers (1.5 horas)

**Objetivo:** Implementar procesamiento asíncrono

**Tareas:**
- [ ] Crear MensajeScheduler.java (@Scheduled fixedRate=60000)
- [ ] Crear QueueProcessorScheduler.java (@Scheduled fixedRate=5000)
- [ ] Configurar @EnableScheduling en clase principal
- [ ] Implementar lógica de reintentos (RN-007, RN-008)
- [ ] Implementar asignación automática (RN-002, RN-003, RN-004)
- [ ] Logging detallado

**Criterios de Aceptación:**
- ✅ MensajeScheduler procesa mensajes pendientes cada 60s
- ✅ QueueProcessorScheduler asigna tickets cada 5s
- ✅ Reintentos funcionan (30s, 60s, 120s backoff)
- ✅ Asignación respeta prioridades y FIFO

**Ejemplo MensajeScheduler.java:**
```java
package com.example.ticketero.scheduler;

import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.repository.MensajeRepository;
import com.example.ticketero.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MensajeScheduler {

    private final MensajeRepository mensajeRepository;
    private final TelegramService telegramService;

    @Scheduled(fixedRate = 60000) // Cada 60 segundos
    @Transactional
    public void procesarMensajesPendientes() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Mensaje> mensajesPendientes = mensajeRepository
            .findByEstadoEnvioAndFechaProgramadaLessThanEqual("PENDIENTE", ahora);

        if (mensajesPendientes.isEmpty()) {
            log.debug("No hay mensajes pendientes");
            return;
        }

        log.info("Procesando {} mensajes pendientes", mensajesPendientes.size());

        for (Mensaje mensaje : mensajesPendientes) {
            try {
                enviarMensaje(mensaje);
            } catch (Exception e) {
                log.error("Error procesando mensaje {}: {}", mensaje.getId(), e.getMessage());
            }
        }
    }

    private void enviarMensaje(Mensaje mensaje) {
        try {
            // Enviar mensaje vía Telegram
            String messageId = telegramService.enviarMensaje(
                mensaje.getTicket().getTelefono(),
                construirTextoMensaje(mensaje)
            );
            
            // Marcar como enviado
            mensaje.setEstadoEnvio("ENVIADO");
            mensaje.setFechaEnvio(LocalDateTime.now());
            mensaje.setTelegramMessageId(messageId);
            
            log.info("Mensaje {} enviado exitosamente", mensaje.getId());
            
        } catch (Exception e) {
            // Manejar reintentos (RN-007, RN-008)
            manejarReintento(mensaje, e);
        }
    }

    private void manejarReintento(Mensaje mensaje, Exception error) {
        mensaje.setIntentos(mensaje.getIntentos() + 1);
        
        if (mensaje.getIntentos() >= 4) {
            // Máximo 3 reintentos alcanzado
            mensaje.setEstadoEnvio("FALLIDO");
            log.error("Mensaje {} falló después de 3 reintentos: {}", 
                mensaje.getId(), error.getMessage());
        } else {
            // Programar reintento con backoff exponencial
            int delayMinutes = switch (mensaje.getIntentos()) {
                case 2 -> 1;  // 30 segundos después del primer fallo
                case 3 -> 2;  // 60 segundos después del segundo fallo
                case 4 -> 4;  // 120 segundos después del tercer fallo
                default -> 1;
            };
            
            mensaje.setFechaProgramada(LocalDateTime.now().plusMinutes(delayMinutes));
            log.warn("Reintento {} programado para mensaje {} en {} minutos", 
                mensaje.getIntentos(), mensaje.getId(), delayMinutes);
        }
    }

    private String construirTextoMensaje(Mensaje mensaje) {
        // Construir texto según plantilla
        return switch (mensaje.getPlantilla()) {
            case totem_ticket_creado -> String.format(
                "✅ Ticket %s creado. Posición: #%d. Tiempo estimado: %d min",
                mensaje.getTicket().getNumero(),
                mensaje.getTicket().getPositionInQueue(),
                mensaje.getTicket().getEstimatedWaitMinutes()
            );
            case totem_proximo_turno -> String.format(
                "⏰ Pronto será tu turno %s. Acércate a la sucursal",
                mensaje.getTicket().getNumero()
            );
            case totem_es_tu_turno -> String.format(
                "🔔 ES TU TURNO %s! Módulo %d, Asesor: %s",
                mensaje.getTicket().getNumero(),
                mensaje.getTicket().getAssignedModuleNumber(),
                mensaje.getTicket().getAssignedAdvisor().getName()
            );
        };
    }
}
```

---

## 6. Orden de Ejecución Recomendado

### Día 1 (4 horas):
```
├── Fase 0: Setup (30 min)
├── Fase 1: Migraciones (45 min)
├── Fase 2: Entities (1 hora)
├── Fase 3: DTOs (45 min)
└── Fase 4: Repositories (30 min)
```

### Día 2 (5 horas):
```
├── Fase 5: Services (3 horas)
├── Fase 6: Controllers (2 horas)
```

### Día 3 (2 horas):
```
└── Fase 7: Schedulers (1.5 horas)
└── Testing E2E (30 min)
```

**TOTAL:** ~11 horas de implementación

---

## 7. Comandos Útiles

### Maven
```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Empaquetar (sin tests)
mvn clean package -DskipTests

# Ejecutar aplicación
mvn spring-boot:run
```

### Docker
```bash
# Levantar PostgreSQL solo
docker-compose up -d postgres

# Ver logs
docker-compose logs -f postgres

# Levantar todo (PostgreSQL + API)
docker-compose up --build

# Detener y limpiar
docker-compose down -v
```

### PostgreSQL
```bash
# Conectar a base de datos
docker exec -it ticketero-db psql -U dev -d ticketero

# Ver tablas
\dt

# Ver migraciones
SELECT * FROM flyway_schema_history;

# Ver asesores
SELECT * FROM advisor;
```

### Testing Manual
```bash
# Health check
curl http://localhost:8080/actuator/health

# Crear ticket
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "nationalId": "12345678-9",
    "telefono": "+56912345678",
    "branchOffice": "Sucursal Centro",
    "queueType": "PERSONAL_BANKER"
  }' | jq

# Obtener dashboard
curl http://localhost:8080/api/admin/dashboard | jq
```

---

## 8. Troubleshooting

### Problemas Comunes

**Error: "Table 'ticket' doesn't exist"**
- Solución: Verificar que Flyway ejecutó las migraciones
- Comando: `SELECT * FROM flyway_schema_history;`

**Error: "Could not connect to PostgreSQL"**
- Solución: Verificar que PostgreSQL está corriendo
- Comando: `docker-compose ps`

**Error: "Validation failed"**
- Solución: Verificar que @Valid está en el controller
- Verificar que GlobalExceptionHandler está configurado

**Error: "Lombok not working"**
- Solución: Verificar plugin Lombok en IDE
- Rebuild project: `mvn clean compile`

---

## 9. Checklist Final de Validación

### Funcionalidad Core
- [ ] Crear ticket funciona (POST /api/tickets)
- [ ] Consultar ticket funciona (GET /api/tickets/{uuid})
- [ ] Dashboard funciona (GET /api/admin/dashboard)
- [ ] Migraciones aplicadas correctamente
- [ ] 5 asesores iniciales creados

### Schedulers
- [ ] MensajeScheduler ejecuta cada 60s
- [ ] QueueProcessorScheduler ejecuta cada 5s
- [ ] Logs muestran procesamiento

### Base de Datos
- [ ] 3 tablas creadas (ticket, mensaje, advisor)
- [ ] Índices creados correctamente
- [ ] Foreign keys funcionando

### Configuración
- [ ] Application.yml configurado
- [ ] Docker-compose funciona
- [ ] Health endpoint responde

### Código
- [ ] Todas las clases compilan
- [ ] Lombok funciona correctamente
- [ ] Validaciones Bean Validation activas
- [ ] Exception handling configurado

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2025  
**Estado:** Listo para Implementación

**Preparado por:** Amazon Q Developer  
**Tiempo estimado total:** 11 horas  
**Complejidad:** Media

Este plan es ejecutable paso a paso por un desarrollador mid-level sin necesidad de consultar documentación adicional. Cada fase es independiente y verificable.