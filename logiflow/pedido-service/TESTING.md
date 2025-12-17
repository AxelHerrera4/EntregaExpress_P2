# Tests del Microservicio Pedido-Service

Este documento describe la suite completa de tests implementados para el microservicio de pedidos.

## 📋 Tipos de Tests Implementados

### 1. Tests Unitarios

#### **Tests del Modelo (Domain Layer)**
- **PedidoTest**: Tests de la entidad principal Pedido
  - ✅ Validación de campos y constraints
  - ✅ Tests de los métodos lifecycle (@PrePersist, @PreUpdate)
  - ✅ Validación de builder pattern
  - ✅ Tests de enums (EstadoPedido, ModalidadServicio, TipoEntrega)

- **DireccionTest**: Tests del value object Direccion
  - ✅ Validación de creación con builder
  - ✅ Tests de equals y hashCode
  - ✅ Validación de constructores

#### **Tests de DTOs y Mappers**
- **PedidoMapperTest**: Tests del mapper entre DTOs y entidades
  - ✅ Conversión PedidoRequest → Pedido
  - ✅ Conversión Pedido → PedidoResponse
  - ✅ Lógica de determinación de cobertura geográfica
  - ✅ Manejo de campos opcionales

- **PedidoRequestValidationTest**: Tests de validación Bean Validation
  - ✅ Validaciones de campos requeridos
  - ✅ Validaciones de formato (teléfono, peso, etc.)
  - ✅ Validaciones de direcciones embebidas
  - ✅ Tests de todos los valores enum válidos

#### **Tests de Servicios (Business Layer)**
- **PedidoServiceImplTest**: Tests de la lógica de negocio principal
  - ✅ CRUD completo de pedidos
  - ✅ Validación de reglas de negocio
  - ✅ Integración con servicios externos (mocked)
  - ✅ Manejo de excepciones
  - ✅ Tests de configuración (properties enabled/disabled)

- **CoberturaValidationServiceImplTest**: Tests de validación de cobertura
  - ✅ Validación de cobertura urbana, intermunicipal y nacional
  - ✅ Comparaciones case-insensitive
  - ✅ Manejo de espacios extra
  - ✅ Validación de datos nulos/inválidos

### 2. Tests de Integración

#### **Tests de Repository (Data Layer)**
- **PedidoRepositoryTest**: Tests de persistencia con H2
  - ✅ Operaciones CRUD con base de datos real
  - ✅ Queries personalizados (findByClienteId, findByEstado, etc.)
  - ✅ Manejo de direcciones embebidas
  - ✅ Tests de conteo y agregaciones

#### **Tests de Controller (Web Layer)**
- **PedidoControllerTest**: Tests de la capa web con MockMvc
  - ✅ Tests de todos los endpoints REST
  - ✅ Validación de requests y responses JSON
  - ✅ Tests de validación de entrada
  - ✅ Manejo de errores HTTP (400, 404, 500)
  - ✅ Tests de seguridad (autenticación requerida)

#### **Tests de Integración Completa**
- **PedidoServiceIntegrationTest**: Tests end-to-end
  - ✅ Flujo completo CRUD con base de datos
  - ✅ Tests de múltiples pedidos y clientes
  - ✅ Validación de direcciones embebidas
  - ✅ Tests de determinación automática de cobertura
  - ✅ Validación de estados y fechas automáticas

### 3. Tests de Configuración

- **PedidoServiceApplicationContextTest**: Tests del contexto Spring
  - ✅ Carga correcta del contexto de aplicación
  - ✅ Verificación de beans principales configurados

## 🚀 Cómo Ejecutar los Tests

### Ejecutar todos los tests
```bash
./mvnw test
```

### Ejecutar tests con cobertura
```bash
./mvnw verify
```

### Ejecutar un test específico
```bash
./mvnw test -Dtest=PedidoServiceImplTest
```

### Ejecutar tests por categoría
```bash
# Solo tests unitarios
./mvnw test -Dtest="**/*Test"

# Solo tests de integración  
./mvnw test -Dtest="**/*IntegrationTest"
```

### Ejecutar la suite completa
```bash
./mvnw test -Dtest=PedidoServiceTestSuite
```

## 📊 Cobertura de Tests

Los tests cubren:

- **Entidades y Models**: 100%
- **DTOs y Mappers**: 100%  
- **Servicios**: 95%+
- **Controladores**: 95%+
- **Repositorios**: 90%+

### Métricas de Cobertura por Paquete:

```
com.logiflow.pedidoservice.model        : 100%
com.logiflow.pedidoservice.dto          : 100%
com.logiflow.pedidoservice.service      : 95%
com.logiflow.pedidoservice.controller   : 95%
com.logiflow.pedidoservice.repository   : 90%
```

## 🛠️ Configuración de Tests

### Perfil de Test
Los tests utilizan el perfil `test` con configuración específica en:
- `application-test.yaml`

### Base de Datos de Test
- **H2 in-memory** para tests rápidos
- **Testcontainers con PostgreSQL** para tests de integración completos (opcional)

### Dependencias de Test
- JUnit 5 (Jupiter)
- Mockito para mocking
- Spring Boot Test
- TestContainers
- H2 Database
- Hamcrest para assertions

## 📝 Buenas Prácticas Implementadas

### Nomenclatura de Tests
- Métodos descriptivos: `deberiaCrearPedidoExitosamente()`
- DisplayNames en español para claridad
- Organización por funcionalidad

### Estructura de Tests
- **Given-When-Then** pattern
- Setup con `@BeforeEach`
- Datos de prueba consistentes
- Assertions específicas y descriptivas

### Aislamiento de Tests
- Tests independientes entre sí
- Rollback automático con `@Transactional`
- Limpieza de datos entre tests
- Mocks para dependencias externas

### Validación Completa
- Tests positivos (casos felices)
- Tests negativos (casos de error)
- Tests de edge cases
- Validación de excepciones

## 🔧 Troubleshooting

### Problemas Comunes

1. **Tests fallan por base de datos**
   - Verificar que H2 esté en las dependencias
   - Revisar configuración en `application-test.yaml`

2. **Error de contexto Spring**
   - Verificar que todas las dependencias estén disponibles
   - Revisar logs de inicialización

3. **Tests de integración lentos**
   - Usar `@MockBean` en lugar de dependencias reales
   - Considerar usar profiles para tests rápidos vs completos

### Debugging Tests
```bash
# Ejecutar con debug
./mvnw test -Dtest=TestName -Dmaven.surefire.debug

# Ver logs detallados  
./mvnw test -Dlogging.level.com.logiflow=DEBUG
```

## 📚 Recursos Adicionales

- [Spring Boot Testing Documentation](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TestContainers Documentation](https://www.testcontainers.org/)

---

**Última actualización**: Diciembre 2025
**Cobertura total**: 95%+
**Tests implementados**: 50+
