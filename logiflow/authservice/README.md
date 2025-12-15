# AuthService

Este proyecto proporciona endpoints de autenticación y recursos protegidos.

## Swagger UI

Una vez levantada la aplicación, accede a:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Para probar endpoints protegidos en Swagger:
- Haz clic en "Authorize".
- Escribe `Bearer <tu-token-jwt>` (sin comillas) en el cuadro y confirma.

## Consumo con Postman (o cliente similar)

Ejemplos de requests:

1. Registro:
- Método: POST
- URL: `http://localhost:8080/api/auth/register`
- Body (JSON):
```
{
  "username": "user1",
  "password": "secret",
  "email": "user1@example.com"
}
```
- Respuesta incluye `accessToken` y `refreshToken`.

2. Login:
- Método: POST
- URL: `http://localhost:8080/api/auth/login`
- Body (JSON):
```
{
  "username": "user1",
  "password": "secret"
}
```
- Copia `accessToken` del response.

3. Acceso a recurso protegido:
- Método: GET
- URL: `http://localhost:8080/api/protected/me`
- Headers:
  - `Authorization: Bearer <accessToken>`

4. Endpoint solo para ADMIN:
- Método: GET
- URL: `http://localhost:8080/api/protected/admin-only`
- Headers:
  - `Authorization: Bearer <accessToken>`
- Debes usar un token con rol `ADMIN`.

5. Refrescar token:
- Método: POST
- URL: `http://localhost:8080/api/auth/token/refresh`
- Body: cadena con el `refreshToken`

## Errores esperados
- 401 Unauthorized: cuando no envías token o es inválido/expirado.
- 403 Forbidden: cuando el token no contiene el rol necesario.

## Cómo ejecutar

Requisitos: Java 21 y Maven.

```sh
./mvnw spring-boot:run
```

## Pruebas

Se incluyen pruebas con JUnit y MockMvc que cubren:
- Rechazo 401 para petición no autenticada.
- Rechazo 403 para petición sin permisos.

Para ejecutar:

```sh
./mvnw test
```

