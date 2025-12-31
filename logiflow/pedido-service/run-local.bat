@echo off
echo ======================================
echo  EJECUTAR PEDIDO-SERVICE EN LOCAL
echo ======================================
echo.

echo 1. Verificando que Docker Compose este ejecutandose...
docker compose ps postgres-pedido
if errorlevel 1 (
    echo ERROR: La base de datos PostgreSQL no esta ejecutandose.
    echo.
    echo Por favor ejecuta primero:
    echo   docker compose up postgres-pedido -d
    echo.
    pause
    exit /b 1
)

echo.
echo 2. Base de datos disponible. Ejecutando pedido-service...
echo.

REM Establecer variables de entorno para desarrollo local
set SPRING_PROFILES_ACTIVE=local
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/pedidos_db
set SPRING_DATASOURCE_USERNAME=pedido_user
set SPRING_DATASOURCE_PASSWORD=pedido_pass

echo Variables de entorno configuradas:
echo   SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%
echo   SPRING_DATASOURCE_URL=%SPRING_DATASOURCE_URL%
echo.

REM Ejecutar la aplicacion
cd /d "%~dp0"
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

echo.
echo Aplicacion finalizada.
pause
