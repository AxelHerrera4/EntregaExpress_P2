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
