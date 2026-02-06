import { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { authManager } from './auth-manager';

/**
 * Configurador de interceptors para clientes HTTP
 * Agrega automáticamente Authorization headers y maneja errores 401/403
 */
export class HttpInterceptors {
  /**
   * Configura interceptors para un cliente HTTP
   */
  public static setupInterceptors(client: AxiosInstance, serviceName: string): void {
    // Interceptor de peticiones (request)
    client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const authHeader = authManager.getAuthHeader();
        
        if (authHeader) {
          config.headers = config.headers || {};
          config.headers['Authorization'] = authHeader;
          console.log(`[${serviceName}] 📤 Request con token: ${config.method?.toUpperCase()} ${config.url}`);
        } else {
          console.warn(`[${serviceName}] ⚠️ Request SIN token: ${config.method?.toUpperCase()} ${config.url}`);
        }
        
        return config;
      },
      (error) => {
        console.error(`[${serviceName}] ❌ Error en request interceptor:`, error);
        return Promise.reject(error);
      }
    );

    // Interceptor de respuestas (response)
    client.interceptors.response.use(
      (response: AxiosResponse) => {
        // Respuesta exitosa
        console.log(`[${serviceName}] ✅ Response: ${response.status} ${response.config.method?.toUpperCase()} ${response.config.url}`);
        return response;
      },
      async (error) => {
        const originalRequest = error.config;
        const status = error.response?.status;

        console.error(`[${serviceName}] ❌ Error Response: ${status} ${originalRequest?.method?.toUpperCase()} ${originalRequest?.url}`);

        // Si es 401 o 403 y no hemos reintentado ya
        if ((status === 401 || status === 403) && !originalRequest._retry) {
          console.log(`[${serviceName}] 🔄 Token expirado, reautenticando...`);
          originalRequest._retry = true;

          try {
            // Fuerza reautenticación
            await authManager.forceReauth();
            
            // Actualiza el header de autorización en la petición original
            const newAuthHeader = authManager.getAuthHeader();
            if (newAuthHeader) {
              originalRequest.headers['Authorization'] = newAuthHeader;
              console.log(`[${serviceName}] 🔄 Reintentando petición con nuevo token...`);
              return client(originalRequest);
            }
          } catch (reAuthError) {
            console.error(`[${serviceName}] ❌ Error al reautenticar:`, reAuthError);
            return Promise.reject(reAuthError);
          }
        }

        // Para otros errores o si la reautenticación falló
        if (error.response?.data) {
          console.error(`[${serviceName}] Error data:`, error.response.data);
        }
        
        return Promise.reject(error);
      }
    );

    console.log(`[HttpInterceptors] ✅ Interceptors configurados para ${serviceName}`);
  }
}

/**
 * Configuración de interceptors para todos los servicios
 */
export function setupAllInterceptors(clients: {
  pedidoClient: AxiosInstance;
  fleetClient: AxiosInstance;
  trackingClient: AxiosInstance;
  authClient: AxiosInstance;
  billingClient: AxiosInstance;
}): void {
  console.log('[HttpInterceptors] Configurando interceptors para todos los clientes...');
  
  HttpInterceptors.setupInterceptors(clients.pedidoClient, 'PedidoService');
  HttpInterceptors.setupInterceptors(clients.fleetClient, 'FleetService');
  HttpInterceptors.setupInterceptors(clients.trackingClient, 'TrackingService');
  HttpInterceptors.setupInterceptors(clients.authClient, 'AuthService');
  HttpInterceptors.setupInterceptors(clients.billingClient, 'BillingService');
  
  console.log('[HttpInterceptors] ✅ Todos los interceptors configurados');
}