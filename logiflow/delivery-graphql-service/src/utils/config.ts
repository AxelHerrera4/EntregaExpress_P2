import dotenv from 'dotenv';

dotenv.config();

export const config = {
  port: parseInt(process.env.PORT || '4000', 10),

  // URLs de los microservicios Java
  pedidoServiceUrl: process.env.PEDIDO_SERVICE_URL || 'http://localhost:8084',
  fleetServiceUrl: process.env.FLEET_SERVICE_URL || 'http://localhost:8083',
  trackingServiceUrl: process.env.TRACKING_SERVICE_URL || 'http://localhost:8090',

  // Timeout para llamadas HTTP (ms)
  httpTimeout: parseInt(process.env.HTTP_TIMEOUT || '5000', 10),
};
