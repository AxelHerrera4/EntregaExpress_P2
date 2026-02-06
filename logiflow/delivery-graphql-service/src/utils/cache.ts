/**
 * Sistema de caché en memoria con métricas (hit/miss) para resolvers
 * En producción se puede reemplazar con Redis
 */
export interface CacheMetrics {
  hits: number;
  misses: number;
  total: number;
  hitRate: number;
}

export class SimpleCache<T> {
  private cache: Map<string, { value: T; timestamp: number }>;
  private ttlMs: number;
  private hits: number = 0;
  private misses: number = 0;

  constructor(ttlMs: number = 60000) {
    // TTL por defecto: 60 segundos
    this.cache = new Map();
    this.ttlMs = ttlMs;
  }

  /**
   * Obtiene un valor del caché
   */
  get(key: string): T | null {
    const entry = this.cache.get(key);

    if (!entry) {
      this.misses++;
      return null;
    }

    // Verificar si expiró
    if (Date.now() - entry.timestamp > this.ttlMs) {
      this.cache.delete(key);
      this.misses++;
      return null;
    }

    this.hits++;
    return entry.value;
  }

  /**
   * Guarda un valor en el caché
   */
  set(key: string, value: T): void {
    this.cache.set(key, {
      value,
      timestamp: Date.now(),
    });
  }

  /**
   * Obtiene o calcula un valor (patrón get-or-compute)
   */
  async getOrCompute(key: string, computeFn: () => Promise<T>): Promise<T> {
    const cached = this.get(key);
    if (cached !== null) {
      return cached;
    }

    const value = await computeFn();
    this.set(key, value);
    return value;
  }

  /**
   * Obtiene métricas del caché
   */
  getMetrics(): CacheMetrics {
    const total = this.hits + this.misses;
    const hitRate = total > 0 ? (this.hits / total) * 100 : 0;

    return {
      hits: this.hits,
      misses: this.misses,
      total,
      hitRate: Math.round(hitRate * 100) / 100, // 2 decimales
    };
  }

  /**
   * Resetea las métricas
   */
  resetMetrics(): void {
    this.hits = 0;
    this.misses = 0;
  }

  /**
   * Limpia el caché completamente
   */
  clear(): void {
    this.cache.clear();
  }

  /**
   * Obtiene el tamaño actual del caché
   */
  size(): number {
    return this.cache.size;
  }
}

// Instancias globales de caché para diferentes recursos
export const flotaCache = new SimpleCache<any>(30000); // 30 segundos
export const kpiCache = new SimpleCache<any>(60000); // 60 segundos
export const pedidoCache = new SimpleCache<any>(20000); // 20 segundos
