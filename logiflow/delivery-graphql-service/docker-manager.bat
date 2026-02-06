@echo off
REM =====================================================
REM Script para gestionar el GraphQL Service con Docker
REM =====================================================

if "%1" == "" goto help

if "%1" == "up" goto up
if "%1" == "down" goto down
if "%1" == "logs" goto logs
if "%1" == "db-only" goto db_only
if "%1" == "clean" goto clean
if "%1" == "redis-cli" goto redis_cli
if "%1" == "psql" goto psql
if "%1" == "help" goto help

:help
echo.
echo ===== LogiFlow GraphQL Service - Docker Manager =====
echo.
echo Uso: %0 [comando]
echo.
echo Comandos disponibles:
echo   up        - Levantar todo el stack
echo   down      - Parar los servicios
echo   db-only   - Solo bases de datos (Redis + PostgreSQL)
echo   logs      - Ver logs en tiempo real
echo   clean     - Parar y limpiar volúmenes
echo   redis-cli - Acceder a Redis CLI
echo   psql      - Acceder a PostgreSQL
echo   help      - Mostrar esta ayuda
echo.
goto end

:up
echo Levantando GraphQL Service completo...
docker-compose up -d
echo.
echo ✅ Servicios disponibles:
echo   - GraphQL: http://localhost:4000
echo   - Redis: localhost:6379
echo   - PostgreSQL: localhost:5436
echo.
goto end

:down
echo Parando servicios...
docker-compose down
goto end

:db_only
echo Levantando solo bases de datos...
docker-compose up redis-cache postgres-graphql -d
echo.
echo ✅ Bases de datos disponibles:
echo   - Redis: localhost:6379
echo   - PostgreSQL: localhost:5436
echo.
goto end

:logs
echo Mostrando logs en tiempo real...
docker-compose logs -f
goto end

:clean
echo Limpiando servicios y volúmenes...
docker-compose down -v
echo ✅ Limpieza completada
goto end

:redis_cli
echo Conectando a Redis...
docker-compose exec redis-cache redis-cli
goto end

:psql
echo Conectando a PostgreSQL...
docker-compose exec postgres-graphql psql -U graphql_user -d graphql_analytics
goto end

:end