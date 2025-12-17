# 🔐 Auth Service (Puerto 8081)

**Servicio de Autenticación y Autorización**

Servicio encargado de la **autenticación y autorización** de usuarios. Maneja el registro de usuarios, login, generación de tokens JWT y validación de credenciales para toda la plataforma.

---

## ⚙️ Configuración Técnica

**Base de Datos:**
- **Motor:** PostgreSQL
- **Host:** `localhost`
- **Puerto:** `5432`
- **Base de datos:** `db_auth`
- **Usuario:** `postgres`
- **Contraseña:** `postgres`

**Tecnologías:**
- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Security** (Autenticación JWT)
- **Spring Data JPA** (ORM)
- **JWT (JSON Web Tokens)** (Autenticación sin estado)

---

## 📋 Diagrama Entidad-Relación (ER)

**Base de Datos:** `db_auth` • **Puerto:** 5432 • **Usuario:** postgres / **Contraseña:** postgres

```
        ╔═════════════════════════════════════════════════════╗
        ║                    roles                            ║
        ╠═════════════════════════════════════════════════════╣
        ║ id                    BIGINT [PK] [IDENTITY]        ║
        ║ name                  VARCHAR(50) [NOT NULL]        ║
        ║                       (ENUM: ADMIN, USER,           ║
        ║                        REPARTIDOR, CLIENTE)         ║
        ║ created_at            TIMESTAMP                     ║
        ╚═════════════════════════════════════════════════════╝
                        △
                        │
                        │ M Roles
                        │ N Users
                        │
        ╔═════════════════════════════════════════════════════╗
        ║                 user_roles                          ║
        ║               (JOIN TABLE)                          ║
        ╠═════════════════════════════════════════════════════╣
        ║ user_id          BIGINT [FK/PK] ──┐               ║
        ║ role_id          BIGINT [FK/PK] ──┼──┐            ║
        ║ assigned_at      TIMESTAMP [DEF]  │  │            ║
        ╚═════════════════════════════════════════════════════╝
                        │                  │
                        │                  └─────────────┐
                        │                                 │
        ╔════════════════════════════════════════════════════╗
        ║                    users                           ║
        ╠════════════════════════════════════════════════════╣
        ║ id                 BIGINT [PK] [IDENTITY]          ║
        ║ username           VARCHAR(100) [UQ] [NOT NULL]    ║
        ║ email              VARCHAR(100) [UQ] [NOT NULL]    ║
        ║ password           VARCHAR(255) [NOT NULL]         ║
        ║                    (BCrypt Hash)                   ║
        ║ refresh_token      VARCHAR(500)                    ║
        ║ account_enabled    BOOLEAN [DEFAULT: true]        ║
        ║ account_locked     BOOLEAN [DEFAULT: false]       ║
        ║ created_at         TIMESTAMP [NOT NULL]           ║
        ║ updated_at         TIMESTAMP [NOT NULL]           ║
        ║ last_login_at      TIMESTAMP                      ║
        ╚════════════════════════════════════════════════════╝

ENUMERADOS ROLE NAME:
┌──────────────────────────────────────────────────┐
│ Valor              │ Descripción                │
├──────────────────────────────────────────────────┤
│ ADMIN              │ Acceso completo            │
│ USER               │ Usuario estándar           │
│ REPARTIDOR         │ Conductor de entregas      │
│ CLIENTE            │ Cliente final              │
└──────────────────────────────────────────────────┘

SEGURIDAD Y AUTENTICACIÓN:
  ✓ Contraseñas: BCrypt (10+ salts rounds)
  ✓ JWT Token: 24 horas de expiración
  ✓ Refresh Token: 7 días de expiración
  ✓ Token Storage: Base de datos (refresh_token en users)
  ✓ Rate Limiting: Implementado en API Gateway

ÍNDICES PARA OPTIMIZACIÓN:
  ✓ CREATE UNIQUE INDEX idx_users_username ON users(username);
  ✓ CREATE UNIQUE INDEX idx_users_email ON users(email);
  ✓ CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
  ✓ CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
  ✓ CREATE INDEX idx_users_created_at ON users(created_at DESC);

VOLUMEN DE DATOS ESTIMADO:
  • Roles:        ~4 registros (< 1KB)
  • Usuarios:     ~1,000-5,000 registros (≈ 1-3MB)
  • User Roles:   ~2,000-10,000 registros (< 1MB)
  • Total BD:     ≈ 5-10MB con índices
```

ROLES DISPONIBLES:
  • ADMIN      - Administrador del sistema
  • USER       - Usuario estándar
  • REPARTIDOR - Repartidor/conductor
  • CLIENTE    - Cliente
```

---

## 📚 API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/register` | Registrar nuevo usuario |
| `POST` | `/auth/login` | Autenticar usuario |
| `POST` | `/auth/refresh` | Refrescar token JWT |
| `POST` | `/auth/logout` | Cerrar sesión |
| `GET` | `/auth/validate` | Validar token JWT |

---

## 🚀 Cómo Comenzar

### Opción 1: Usando Maven

```bash
cd logiflow/auth-service
./mvnw spring-boot:run
```

### Opción 2: Acceder a Swagger

Una vez iniciado el servicio:

🌐 **URL:** `http://localhost:8081/swagger-ui.html`

---

## 📖 Documentación Adicional

- **Swagger/OpenAPI:** `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8081/v3/api-docs`

---

## 🔐 Características de Seguridad

- ✅ **JWT Token Authentication** - Autenticación sin estado
- ✅ **BCrypt Password Hashing** - Contraseñas seguras
- ✅ **Role-Based Access Control (RBAC)** - Control de acceso por roles
- ✅ **Refresh Token Strategy** - Renovación segura de tokens
- ✅ **Rate Limiting** - Protección contra ataques de fuerza bruta
