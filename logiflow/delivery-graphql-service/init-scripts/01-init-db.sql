-- =====================================================
-- SCRIPT DE INICIALIZACIÓN PARA GRAPHQL ANALYTICS DB
-- =====================================================

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- =====================================================
-- TABLA: query_analytics
-- Almacena métricas de rendimiento de queries GraphQL
-- =====================================================
CREATE TABLE IF NOT EXISTS query_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    query_hash VARCHAR(64) NOT NULL,
    query_name VARCHAR(255),
    query_text TEXT,
    execution_time_ms INTEGER NOT NULL,
    variables_count INTEGER DEFAULT 0,
    complexity_score INTEGER,
    cache_hit BOOLEAN DEFAULT FALSE,
    user_id VARCHAR(100),
    user_role VARCHAR(50),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Índices para consultas rápidas
    INDEX idx_query_analytics_hash (query_hash),
    INDEX idx_query_analytics_name (query_name),
    INDEX idx_query_analytics_created (created_at),
    INDEX idx_query_analytics_execution_time (execution_time_ms),
    INDEX idx_query_analytics_user (user_id)
);

-- =====================================================
-- TABLA: cache_metrics
-- Métricas del sistema de caché
-- =====================================================
CREATE TABLE IF NOT EXISTS cache_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cache_type VARCHAR(50) NOT NULL, -- 'pedido', 'flota', 'kpi'
    cache_key VARCHAR(255) NOT NULL,
    hit_count INTEGER DEFAULT 0,
    miss_count INTEGER DEFAULT 0,
    last_accessed TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ttl_seconds INTEGER,
    size_bytes INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Índices
    INDEX idx_cache_metrics_type (cache_type),
    INDEX idx_cache_metrics_key (cache_key),
    INDEX idx_cache_metrics_accessed (last_accessed)
);

-- =====================================================
-- TABLA: error_logs
-- Logs de errores del GraphQL service
-- =====================================================
CREATE TABLE IF NOT EXISTS error_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    error_type VARCHAR(100) NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    query_hash VARCHAR(64),
    query_name VARCHAR(255),
    user_id VARCHAR(100),
    microservice_source VARCHAR(50), -- 'pedido', 'fleet', 'auth', etc.
    http_status INTEGER,
    request_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Índices
    INDEX idx_error_logs_type (error_type),
    INDEX idx_error_logs_created (created_at),
    INDEX idx_error_logs_microservice (microservice_source),
    INDEX idx_error_logs_user (user_id)
);

-- =====================================================
-- TABLA: microservice_health
-- Estado de salud de los microservicios
-- =====================================================
CREATE TABLE IF NOT EXISTS microservice_health (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_name VARCHAR(50) NOT NULL, -- 'auth-service', 'pedido-service', etc.
    service_url VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'healthy', 'unhealthy', 'unknown'
    response_time_ms INTEGER,
    last_check TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    error_message TEXT,
    consecutive_failures INTEGER DEFAULT 0,
    
    -- Constraint para un registro por servicio
    UNIQUE(service_name),
    
    -- Índices
    INDEX idx_microservice_health_status (status),
    INDEX idx_microservice_health_last_check (last_check)
);

-- =====================================================
-- VISTAS PARA REPORTING
-- =====================================================

-- Vista: Métricas de rendimiento por query
CREATE OR REPLACE VIEW v_query_performance AS
SELECT 
    query_name,
    query_hash,
    COUNT(*) as execution_count,
    AVG(execution_time_ms) as avg_execution_time,
    MIN(execution_time_ms) as min_execution_time,
    MAX(execution_time_ms) as max_execution_time,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY execution_time_ms) as p95_execution_time,
    AVG(CASE WHEN cache_hit THEN 1.0 ELSE 0.0 END) as cache_hit_rate,
    DATE_TRUNC('hour', created_at) as hour_bucket
FROM query_analytics 
WHERE created_at >= NOW() - INTERVAL '24 hours'
GROUP BY query_name, query_hash, DATE_TRUNC('hour', created_at)
ORDER BY avg_execution_time DESC;

-- Vista: Resumen de caché por tipo
CREATE OR REPLACE VIEW v_cache_summary AS
SELECT 
    cache_type,
    COUNT(*) as total_keys,
    SUM(hit_count) as total_hits,
    SUM(miss_count) as total_misses,
    CASE 
        WHEN SUM(hit_count + miss_count) > 0 
        THEN SUM(hit_count)::FLOAT / SUM(hit_count + miss_count) * 100
        ELSE 0 
    END as hit_rate_percentage,
    SUM(size_bytes) as total_size_bytes,
    AVG(ttl_seconds) as avg_ttl_seconds,
    MAX(last_accessed) as last_activity
FROM cache_metrics 
GROUP BY cache_type
ORDER BY hit_rate_percentage DESC;

-- =====================================================
-- FUNCIONES PARA LIMPIEZA AUTOMÁTICA
-- =====================================================

-- Función para limpiar datos antiguos
CREATE OR REPLACE FUNCTION cleanup_old_analytics() RETURNS void AS $$
BEGIN
    -- Eliminar query analytics mayores a 7 días
    DELETE FROM query_analytics 
    WHERE created_at < NOW() - INTERVAL '7 days';
    
    -- Eliminar error logs mayores a 30 días
    DELETE FROM error_logs 
    WHERE created_at < NOW() - INTERVAL '30 days';
    
    -- Eliminar métricas de caché no accedidas en 24 horas
    DELETE FROM cache_metrics 
    WHERE last_accessed < NOW() - INTERVAL '24 hours';
    
    RAISE NOTICE 'Cleanup completed successfully';
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- DATOS INICIALES
-- =====================================================

-- Insertar servicios de microservicio para monitoreo
INSERT INTO microservice_health (service_name, service_url, status, last_check) 
VALUES 
    ('auth-service', 'http://localhost:8081/actuator/health', 'unknown', NOW()),
    ('pedido-service', 'http://localhost:8084/actuator/health', 'unknown', NOW()),
    ('fleet-service', 'http://localhost:8083/actuator/health', 'unknown', NOW()),
    ('tracking-service', 'http://localhost:8090/actuator/health', 'unknown', NOW())
ON CONFLICT (service_name) DO NOTHING;

-- Comentarios en las tablas
COMMENT ON TABLE query_analytics IS 'Almacena métricas de rendimiento de queries GraphQL ejecutadas';
COMMENT ON TABLE cache_metrics IS 'Métricas del sistema de caché para optimización';
COMMENT ON TABLE error_logs IS 'Registro de errores del GraphQL service y microservicios';
COMMENT ON TABLE microservice_health IS 'Estado de salud de los microservicios conectados';

-- =====================================================
-- SCRIPT COMPLETADO EXITOSAMENTE
-- =====================================================
SELECT 'GraphQL Analytics Database initialized successfully!' as status;