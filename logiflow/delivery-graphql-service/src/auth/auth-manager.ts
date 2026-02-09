import axios, { AxiosInstance } from 'axios';
import { config } from '../utils/config';

/**
 * AuthManager - Maneja la autenticación automática del servicio GraphQL
 * Se autentica con credenciales admin/admin123 y mantiene el token actualizado
 * Incluye reintentos automáticos con espera exponencial para manejar servicios que inician lentamente
 */
export class AuthManager {
  private static instance: AuthManager;
  private token: string | null = null;
  private refreshTimer: NodeJS.Timeout | null = null;
  private retryTimer: NodeJS.Timeout | null = null;
  private retryCount: number = 0;
  private readonly maxRetries: number = 30; // Máximo 30 reintentos = ~5 minutos
  private readonly initialRetryDelay: number = 3000; // 3 segundos iniciales
  private authenticated: boolean = false;

  private readonly credentials = {
    username: 'admin',
    password: 'admin123'
  };

  // Cliente HTTP directo para autenticación (sin interceptors)
  private authClient: AxiosInstance;

  private constructor() {
    this.authClient = axios.create({
      baseURL: config.authServiceUrl,
      timeout: config.httpTimeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  /**
   * Obtiene la instancia singleton del AuthManager
   */
  public static getInstance(): AuthManager {
    if (!AuthManager.instance) {
      AuthManager.instance = new AuthManager();
    }
    return AuthManager.instance;
  }

  /**
   * Inicia el sistema de autenticación automática con reintentos
   * NO lanza error si falla inicialmente, solo inicia reintentos automáticos
   */
  public async initialize(): Promise<void> {
    console.log('[AuthManager] Iniciando autenticación automática con reintentos...');
    
    // Intenta login inmediatamente
    const loginSuccess = await this.attemptLogin();
    
    if (loginSuccess) {
      console.log('[AuthManager] ✅ Autenticación automática iniciada');
      this.scheduleTokenRefresh();
    } else {
      // Si falla, programa reintentos automáticos
      console.log('[AuthManager] ⚠️ Login fallido. Programando reintentos automáticos...');
      this.scheduleRetry();
    }
  }

  /**
   * Intenta realizar login una sola vez
   * Retorna true si éxito, false si falla
   */
  private async attemptLogin(): Promise<boolean> {
    try {
      console.log(`[AuthManager] Intento ${this.retryCount + 1}/${this.maxRetries}: Realizando login con usuario: ${this.credentials.username}`);
      
      const response = await this.authClient.post('/login', {
        username: this.credentials.username,
        password: this.credentials.password
      });

      if (response.data && response.data.accessToken) {
        this.token = response.data.accessToken;
        this.authenticated = true;
        this.retryCount = 0; // Reset retry counter on success
        
        console.log('[AuthManager] ✅ Token JWT obtenido correctamente');
        console.log(`[AuthManager] Usuario: ${response.data.username}`);
        console.log(`[AuthManager] Email: ${response.data.email}`);
        console.log(`[AuthManager] Roles: ${response.data.roles?.join(', ')}`);
        console.log(`[AuthManager] Token: ${this.token?.substring(0, 50)}...`);
        
        return true;
      } else {
        throw new Error('Respuesta de login inválida - no se recibió accessToken');
      }
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || error.message || 'Error desconocido';
      console.error(`[AuthManager] Error en intento ${this.retryCount + 1}: ${errorMsg}`);
      return false;
    }
  }

  /**
   * Programa el siguiente reintento con espera exponencial
   */
  private scheduleRetry(): void {
    if (this.authenticated) {
      // Ya está autenticado, no necesita reintentos
      return;
    }

    if (this.retryCount >= this.maxRetries) {
      console.error('[AuthManager] ❌ Se alcanzó el máximo de reintentos. Abortando reintentos.');
      return;
    }

    // Espera exponencial: 3s, 6s, 12s, 24s... limitado a 2 minutos max
    const delay = Math.min(
      this.initialRetryDelay * Math.pow(2, this.retryCount),
      2 * 60 * 1000 // Máximo 2 minutos
    );

    if (this.retryTimer) {
      clearTimeout(this.retryTimer);
    }

    console.log(`[AuthManager] Próximo reintento en ${Math.round(delay / 1000)} segundos...`);

    this.retryTimer = setTimeout(async () => {
      this.retryCount++;
      const success = await this.attemptLogin();
      
      if (success) {
        this.scheduleTokenRefresh();
      } else {
        this.scheduleRetry(); // Programa el siguiente reintento
      }
    }, delay);
  }

  /**
   * Programa la renovación automática del token (cada 50 minutos)
   */
  private scheduleTokenRefresh(): void {
    // Renovar token cada 50 minutos (asumiendo que expira en 1 hora)
    const refreshInterval = 50 * 60 * 1000; // 50 minutos en milisegundos

    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
    }

    this.refreshTimer = setTimeout(async () => {
      try {
        console.log('[AuthManager] Renovando token automáticamente...');
        const success = await this.attemptLogin();
        
        if (success) {
          this.scheduleTokenRefresh(); // Programa la siguiente renovación
        } else {
          console.error('[AuthManager] Error al renovar token. Reintentando en 5 minutos...');
          setTimeout(() => {
            this.scheduleTokenRefresh();
          }, 5 * 60 * 1000);
        }
      } catch (error) {
        console.error('[AuthManager] Excepción al renovar token:', error);
        // En caso de error, reintenta en 5 minutos
        setTimeout(() => {
          this.scheduleTokenRefresh();
        }, 5 * 60 * 1000);
      }
    }, refreshInterval);

    console.log(`[AuthManager] Próxima renovación de token en ${refreshInterval / 60000} minutos`);
  }

  /**
   * Obtiene el token JWT actual
   */
  public getToken(): string | null {
    return this.token;
  }

  /**
   * Obtiene el header Authorization con Bearer token
   */
  public getAuthHeader(): string | null {
    if (!this.token) {
      console.warn('[AuthManager] ⚠️ Solicitado token pero no está disponible');
      return null;
    }
    return `Bearer ${this.token}`;
  }

  /**
   * Verifica si hay un token válido
   */
  public isAuthenticated(): boolean {
    return this.authenticated && this.token !== null;
  }

  /**
   * Cierra el sistema de autenticación
   */
  public shutdown(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
    if (this.retryTimer) {
      clearTimeout(this.retryTimer);
      this.retryTimer = null;
    }
    this.token = null;
    this.authenticated = false;
    console.log('[AuthManager] Sistema de autenticación cerrado');
  }

  /**
   * Fuerza una nueva autenticación (para manejar tokens expirados)
   */
  public async forceReauth(): Promise<void> {
    console.log('[AuthManager] Forzando nueva autenticación...');
    const success = await this.attemptLogin();
    
    if (!success) {
      // Si falla, programa reintentos
      console.log('[AuthManager] Fuerza de reauth falló, programando reintentos...');
      this.scheduleRetry();
    }
  }
}

// Exportar instancia singleton
export const authManager = AuthManager.getInstance();