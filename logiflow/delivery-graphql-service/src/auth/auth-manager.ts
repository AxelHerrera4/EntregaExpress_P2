import axios, { AxiosInstance } from 'axios';
import { config } from '../utils/config';

/**
 * AuthManager - Maneja la autenticación automática del servicio GraphQL
 * Se autentica con credenciales admin/admin123 y mantiene el token actualizado
 */
export class AuthManager {
  private static instance: AuthManager;
  private token: string | null = null;
  private refreshTimer: NodeJS.Timeout | null = null;
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
   * Inicia el sistema de autenticación automática
   */
  public async initialize(): Promise<void> {
    console.log('[AuthManager] Iniciando autenticación automática...');
    try {
      await this.login();
      this.scheduleTokenRefresh();
      console.log('[AuthManager] ✅ Autenticación automática iniciada');
    } catch (error) {
      console.error('[AuthManager] ❌ Error al inicializar autenticación:', error);
      throw new Error('Error al inicializar sistema de autenticación');
    }
  }

  /**
   * Realiza login y obtiene el token JWT
   */
  private async login(): Promise<void> {
    try {
      console.log(`[AuthManager] Realizando login con usuario: ${this.credentials.username}`);
      
      const response = await this.authClient.post('/login', {
        username: this.credentials.username,
        password: this.credentials.password
      });

      if (response.data && response.data.accessToken) {
        this.token = response.data.accessToken;
        console.log('[AuthManager] ✅ Token JWT obtenido correctamente');
        console.log(`[AuthManager] Usuario: ${response.data.username}`);
        console.log(`[AuthManager] Email: ${response.data.email}`);
        console.log(`[AuthManager] Roles: ${response.data.roles?.join(', ')}`);
        console.log(`[AuthManager] Token: ${this.token?.substring(0, 50)}...`);
      } else {
        throw new Error('Respuesta de login inválida - no se recibió accessToken');
      }
    } catch (error: any) {
      console.error('[AuthManager] Error en login:', error.response?.data || error.message);
      throw error;
    }
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
        await this.login();
        this.scheduleTokenRefresh(); // Programa la siguiente renovación
      } catch (error) {
        console.error('[AuthManager] Error al renovar token:', error);
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
    return this.token !== null;
  }

  /**
   * Cierra el sistema de autenticación
   */
  public shutdown(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
    this.token = null;
    console.log('[AuthManager] Sistema de autenticación cerrado');
  }

  /**
   * Fuerza una nueva autenticación (para manejar tokens expirados)
   */
  public async forceReauth(): Promise<void> {
    console.log('[AuthManager] Forzando nueva autenticación...');
    await this.login();
  }
}

// Exportar instancia singleton
export const authManager = AuthManager.getInstance();