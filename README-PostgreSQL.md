# 🐘 PostgreSQL Setup para Desarrollo Local

## 🚀 Opción 1: Docker PostgreSQL (Recomendado)

### 1. Levantar PostgreSQL
```bash
# Iniciar PostgreSQL en Docker
docker-compose -f docker-compose.dev.yml up -d

# Verificar que está corriendo
docker ps
```

### 2. Ejecutar aplicación con PostgreSQL
```bash
# Usar perfil postgres
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# O con variable de entorno
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

### 3. Conectar a la base de datos
```bash
# Desde línea de comandos
docker exec -it ticketero-postgres psql -U dev -d ticketero

# O usar herramienta gráfica:
# Host: localhost
# Port: 5432
# Database: ticketero
# User: dev
# Password: dev123
```

### 4. Detener PostgreSQL
```bash
docker-compose -f docker-compose.dev.yml down
```

## 🔧 Opción 2: PostgreSQL Instalado Localmente

### 1. Instalar PostgreSQL
```bash
# Windows (con Chocolatey)
choco install postgresql

# macOS (con Homebrew)
brew install postgresql

# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib
```

### 2. Crear base de datos
```bash
# Crear usuario y base de datos
sudo -u postgres psql
CREATE USER dev WITH PASSWORD 'dev123';
CREATE DATABASE ticketero OWNER dev;
GRANT ALL PRIVILEGES ON DATABASE ticketero TO dev;
\q
```

### 3. Ejecutar aplicación
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## 🎯 Comandos Útiles

### Verificar conexión
```bash
curl http://localhost:8081/actuator/health
```

### Ver tablas creadas
```sql
-- Conectar a la base de datos
\c ticketero

-- Listar tablas
\dt

-- Ver estructura de tabla
\d tickets
```

### Consultar datos
```sql
-- Ver tickets
SELECT * FROM tickets;

-- Ver asesores
SELECT * FROM advisors;

-- Ver mensajes
SELECT * FROM mensajes;
```

## 🔄 Cambiar entre H2 y PostgreSQL

### Usar H2 (desarrollo rápido)
```bash
mvn spring-boot:run
# o
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

### Usar PostgreSQL (testing real)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## 🐛 Troubleshooting

### Error: "Connection refused"
```bash
# Verificar que PostgreSQL está corriendo
docker ps | grep postgres

# Si no está corriendo
docker-compose -f docker-compose.dev.yml up -d
```

### Error: "Database does not exist"
```bash
# Recrear base de datos
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
```

### Ver logs de PostgreSQL
```bash
docker logs ticketero-postgres
```

## 📊 Herramientas Recomendadas

- **pgAdmin**: Interfaz gráfica para PostgreSQL
- **DBeaver**: Cliente universal de base de datos
- **DataGrip**: IDE de JetBrains para bases de datos