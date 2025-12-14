# Fleet Service - Sistema de Gestión de Flota

Microservicio de gestión de vehículos y repartidores para el sistema LogiFlow EntregaExpress.

## 📋 Descripción

El Fleet Service es responsable de gestionar la flota de vehículos y repartidores, proporcionando funcionalidades CRUD completas, métricas de rendimiento y estadísticas operacionales.

## 🚀 Funcionalidades Implementadas - Fase 1

### Gestión de Vehículos
- ✅ CRUD completo de vehículos
- ✅ Clasificación por tipo (Motorizado, Vehículo Liviano, Camión)
- ✅ Gestión de estado (activo/inactivo)
- ✅ Consulta de vehículos disponibles (sin asignar)
- ✅ Factory Pattern para creación de vehículos

### Gestión de Repartidores
- ✅ CRUD completo de repartidores
- ✅ Gestión de estados (DISPONIBLE, EN_RUTA, DESCANSO, etc.)
- ✅ Asignación/desasignación de vehículos
- ✅ Validación de licencias de conducción
- ✅ Gestión de zonas de trabajo
- ✅ Horarios laborales

### Métricas y Estadísticas
- ✅ Estadísticas generales de la flota
- ✅ Métricas individuales por repartidor
- ✅ Top performers (mejores repartidores)
- ✅ Tasa de éxito en entregas
- ✅ Kilómetros recorridos

### Seguridad
- ✅ Autenticación JWT
- ✅ Autorización basada en roles
- ✅ Control de acceso granular por endpoint

### Documentación
- ✅ OpenAPI/Swagger UI integrado
- ✅ Documentación interactiva en `/swagger-ui.html`

## 🏗️ Arquitectura

### Patrones de Diseño
- **Factory Pattern**: Creación de diferentes tipos de vehículos
- **Repository Pattern**: Acceso a datos
- **DTO Pattern**: Separación de entidades y DTOs
- **Service Layer Pattern**: Lógica de negocio

### Tecnologías
- Java 21
- Spring Boot 4.0.0
- Spring Data JPA
- PostgreSQL 13.23
- JWT (io.jsonwebtoken)
- OpenAPI 3 / Swagger
- Lombok
- MapStruct

## 📦 Endpoints Principales

### Vehículos (`/vehiculos`)
```
POST   /vehiculos                    - Crear vehículo
GET    /vehiculos                    - Listar todos
GET    /vehiculos/{id}               - Obtener por ID
GET    /vehiculos/tipo/{tipo}        - Filtrar por tipo
GET    /vehiculos/activos            - Listar activos
GET    /vehiculos/disponibles        - Listar sin asignar
PATCH  /vehiculos/{id}               - Actualizar
PATCH  /vehiculos/{id}/estado        - Cambiar estado
DELETE /vehiculos/{id}               - Eliminar (lógico)
```

### Repartidores (`/repartidores`)
```
POST   /repartidores                    - Crear repartidor
GET    /repartidores                    - Listar todos
GET    /repartidores/{id}               - Obtener por ID
GET    /repartidores/estado/{estado}    - Filtrar por estado
GET    /repartidores/disponibles        - Listar disponibles
GET    /repartidores/zona/{zona}        - Filtrar por zona
GET    /repartidores/{id}/metricas      - Métricas individuales
GET    /repartidores/top-performers     - Top 10 mejores
PATCH  /repartidores/{id}               - Actualizar
PATCH  /repartidores/{id}/estado        - Cambiar estado
POST   /repartidores/{id}/asignar-vehiculo - Asignar vehículo
DELETE /repartidores/{id}/vehiculo      - Remover vehículo
DELETE /repartidores/{id}               - Eliminar (lógico)
```

### Estadísticas (`/estadisticas`)
```
GET    /estadisticas/flota           - Estadísticas generales
```

### Health Check (`/health`)
```
GET    /health                       - Estado del servicio
```

## 🔐 Roles y Permisos

| Rol             | Permisos                                           |
|-----------------|---------------------------------------------------|
| ADMINISTRADOR   | Acceso completo a todos los endpoints             |
| GERENTE         | Gestión completa excepto configuraciones críticas |
| SUPERVISOR      | Consulta y operaciones de gestión                 |
| REPARTIDOR      | Consulta de su información y métricas             |

## 🗄️ Modelo de Datos

### Entidades Principales

#### VehiculoEntrega
- Clase abstracta base para todos los vehículos
- Tipos: Motorizado, VehiculoLiviano, Camion
- Campos: placa, marca, modelo, año, capacidadCarga, estado, etc.

#### Repartidor
- Información personal y profesional
- Tipo de licencia y validación
- Métricas de rendimiento
- Vehículo asignado
- Horarios y zonas de trabajo

## 📊 Métricas Disponibles

### Estadísticas de Flota
- Total de vehículos y repartidores
- Vehículos activos/disponibles
- Repartidores por estado
- Distribución por tipo de vehículo
- Tasa de éxito global

### Métricas por Repartidor
- Entregas completadas/fallidas
- Tasa de éxito personal
- Calificación promedio
- Kilómetros recorridos
- Promedio entregas por día

## 🚦 Configuración

### application.yaml
```yaml
server:
  port: 8082
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fleet_db
    username: fleet_user
    password: fleet_password
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  issuer: auth-service
```

## 🧪 Testing

Para ejecutar las pruebas:
```bash
./mvnw test
```

## 📝 Documentación API

Una vez iniciada la aplicación, accede a:
- Swagger UI: http://localhost:8082/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/api/api-docs

## 🔄 Mejoras Implementadas (Fase 1)

1. ✅ **JPA Auditing habilitado** - Campos de auditoría automáticos
2. ✅ **Endpoints de estadísticas** - Dashboard operacional
3. ✅ **Métricas por repartidor** - Evaluación de desempeño
4. ✅ **Health Check** - Monitoreo del microservicio
5. ✅ **Documentación completa** - OpenAPI/Swagger
6. ✅ **Control de acceso robusto** - Seguridad JWT
7. ✅ **Validaciones de negocio** - Integridad de datos
8. ✅ **Factory Pattern** - Creación flexible de vehículos

## 📅 Próximas Funcionalidades (Fase 2)

- [ ] Integración con Order Service
- [ ] Tracking en tiempo real
- [ ] Optimización de rutas
- [ ] Notificaciones push
- [ ] Dashboard analítico
- [ ] Reportes PDF/Excel

## 👥 Equipo de Desarrollo

Desarrollado para el proyecto de Aplicaciones Distribuidas - Universidad ESPE

---

**Última actualización:** Diciembre 14, 2025
**Versión:** 1.0.0 (Fase 1 Backend)
