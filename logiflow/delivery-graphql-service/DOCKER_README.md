# Docker Compose para GraphQL Service

Este docker-compose levanta la infraestructura completa para el servicio GraphQL de LogiFlow.

## 🐳 Servicios Incluidos

### Redis (Puerto 6379)
- **Propósito**: Caché persistente para consultas GraphQL
- **Imagen**: redis:7.2-alpine
- **Configuración**: Ver `redis.conf`

### PostgreSQL (Puerto 5436)
- **Propósito**: Analytics, logs y métricas del GraphQL service
- **Base de datos**: `graphql_analytics`
- **Usuario**: `graphql_user`
- **Contraseña**: `graphql_pass123`

### GraphQL Service (Puerto 4000)
- **Propósito**: API GraphQL principal
- **Conecta con**: Todos los microservicios Java

## 🚀 Comandos de Uso

### Levantar solo las bases de datos:
```bash
docker-compose up redis-cache postgres-graphql -d
```

### Levantar todo el stack:
```bash
docker-compose up -d
```

### Ver logs en tiempo real:
```bash
docker-compose logs -f graphql-service
```

### Acceder a Redis CLI:
```bash
docker-compose exec redis-cache redis-cli
```

### Acceder a PostgreSQL:
```bash
docker-compose exec postgres-graphql psql -U graphql_user -d graphql_analytics
```

### Parar y limpiar:
```bash
docker-compose down
docker-compose down -v  # También elimina volúmenes
```

## 📊 URLs de Acceso

- **GraphQL Playground**: http://localhost:4000
- **Redis**: localhost:6379
- **PostgreSQL**: localhost:5436

## 🗂️ Estructura de Archivos

```
delivery-graphql-service/
├── docker-compose.yml          # Configuración principal
├── redis.conf                  # Configuración Redis
├── init-scripts/               # Scripts de inicialización DB
│   └── 01-init-db.sql         # Tablas para analytics
├── logs/                       # Logs del servicio
│   └── .gitkeep               # Mantiene directorio en Git
└── Dockerfile                 # Imagen del GraphQL service
```

## 🔧 Variables de Entorno

El servicio GraphQL se conecta automáticamente a:
- **Auth Service**: http://host.docker.internal:8081
- **Pedido Service**: http://host.docker.internal:8084
- **Fleet Service**: http://host.docker.internal:8083
- **Tracking Service**: http://host.docker.internal:8090

## 💾 Persistencia

Los datos se guardan en volúmenes Docker:
- `logiflow_graphql_redis_data`: Datos de Redis
- `logiflow_graphql_postgres_data`: Base de datos PostgreSQL

## 🏥 Health Checks

Todos los servicios tienen health checks configurados:
- **Redis**: `redis-cli ping`
- **PostgreSQL**: `pg_isready`
- **GraphQL**: Query básica GraphQL

## 📈 Monitoreo

La base de datos PostgreSQL incluye tablas para:
- Analytics de queries GraphQL
- Métricas de caché
- Logs de errores
- Estado de microservicios

Ejecutar queries de ejemplo:
```sql
-- Ver rendimiento de queries
SELECT * FROM v_query_performance LIMIT 10;

-- Ver métricas de caché
SELECT * FROM v_cache_summary;
```