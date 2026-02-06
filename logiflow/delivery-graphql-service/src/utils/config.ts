import dotenv from 'dotenv';

dotenv.config();

export const config = {
  port: parseInt(process.env.PORT || '4000', 10),

  // URLs a través del API Gateway (puerto 8000)
  pedidoServiceUrl: process.env.PEDIDO_SERVICE_URL || 'http://localhost:8000/pedido',
  fleetServiceUrl: process.env.FLEET_SERVICE_URL || 'http://localhost:8000/fleet',
  trackingServiceUrl: process.env.TRACKING_SERVICE_URL || 'http://localhost:8090', // Sin gateway por ahora
  authServiceUrl: process.env.AUTH_SERVICE_URL || 'http://localhost:8000/api/auth',
  billingServiceUrl: process.env.BILLING_SERVICE_URL || 'http://localhost:8000',

  // Timeout para llamadas HTTP (ms)
  httpTimeout: parseInt(process.env.HTTP_TIMEOUT || '5000', 10),
};
