import axios, { AxiosInstance } from 'axios';
import { config } from './config';

/**
 * Crea una instancia de Axios preconfigurada
 */
function createClient(baseURL: string): AxiosInstance {
  return axios.create({
    baseURL,
    timeout: config.httpTimeout,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}

/** Cliente HTTP para Pedido Service (puerto 8084) */
export const pedidoClient = createClient(config.pedidoServiceUrl);

/** Cliente HTTP para Fleet Service (puerto 8083) */
export const fleetClient = createClient(config.fleetServiceUrl);

/** Cliente HTTP para Tracking Service (puerto 8090) */
export const trackingClient = createClient(config.trackingServiceUrl);

/** Cliente HTTP para Auth Service (puerto 8081) */
export const authClient = createClient(config.authServiceUrl);

/** Cliente HTTP para Billing Service (puerto 8082) */
export const billingClient = createClient(config.billingServiceUrl);

/**
 * Configura los interceptors de autenticación para todos los clientes
 * DEBE llamarse después de inicializar el AuthManager
 */
export function setupHttpClients(): void {
  // Los interceptors se configuran de forma lazy para evitar dependencias circulares
  const { setupAllInterceptors } = require('../auth');
  
  setupAllInterceptors({
    pedidoClient,
    fleetClient,
    trackingClient,
    authClient,
    billingClient
  });
  
  console.log('[AxiosClients] ✅ Clientes HTTP configurados con autenticación');
}
