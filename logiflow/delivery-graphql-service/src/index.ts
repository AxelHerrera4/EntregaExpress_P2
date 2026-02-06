import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import DataLoader from 'dataloader';
import { typeDefs } from './typeDefs/schema';
import { resolvers } from './resolvers';
import {
  PedidoService,
  FleetServiceClient,
  TrackingServiceClient,
  FlotaService,
  KpiService,
  AuthServiceClient,
  IncidenciaServiceClient,
  BillingServiceClient,
  RepartidorResponse,
  VehiculoResponse,
} from './services';
import { config, createRepartidorLoader, createVehiculoLoader, setupHttpClients } from './utils';
import { authManager } from './auth';

/**
 * Contexto compartido por todos los resolvers
 * Inyecta las instancias de los servicios y DataLoaders para que cada resolver pueda usarlos
 */
export interface GraphQLContext {
  pedidoService: PedidoService;
  fleetClient: FleetServiceClient;
  trackingClient: TrackingServiceClient;
  flotaService: FlotaService;
  kpiService: KpiService;
  authClient: AuthServiceClient;
  incidenciaClient: IncidenciaServiceClient;
  billingClient: BillingServiceClient;
  // DataLoaders para evitar N+1
  repartidorLoader: DataLoader<string, RepartidorResponse | null>;
  vehiculoLoader: DataLoader<string, VehiculoResponse | null>;
}

// Instanciar servicios (singleton)
const pedidoService = new PedidoService();
const fleetClient = new FleetServiceClient();
const trackingClient = new TrackingServiceClient();
const authClient = new AuthServiceClient();
const incidenciaClient = new IncidenciaServiceClient();
const billingClient = new BillingServiceClient();
const flotaService = new FlotaService(fleetClient, trackingClient);
const kpiService = new KpiService(pedidoService, fleetClient);

async function startServer(): Promise<void> {
  try {
    console.log('🔐 Inicializando sistema de autenticación...');
    
    // 1. Inicializar el sistema de autenticación automática
    await authManager.initialize();
    
    // 2. Configurar interceptors para todos los clientes HTTP
    setupHttpClients();

    console.log('🚀 Iniciando servidor GraphQL...');
    
    const server = new ApolloServer<GraphQLContext>({
      typeDefs,
      resolvers,
    });

    const { url } = await startStandaloneServer(server, {
      listen: { port: config.port },
      context: async (): Promise<GraphQLContext> => ({
        pedidoService,
        fleetClient,
        trackingClient,
        flotaService,
        kpiService,
        authClient,
        incidenciaClient,
        billingClient,
        // Crear nuevos DataLoaders por request (importante para evitar cache entre requests)
        repartidorLoader: createRepartidorLoader(fleetClient),
        vehiculoLoader: createVehiculoLoader(fleetClient),
      }),
    });

    console.log('');
    console.log('🎉 ¡LogiFlow GraphQL Service iniciado exitosamente!');
    console.log('');
    console.log(`🚀 Servidor GraphQL listo en ${url}`);
    console.log(`📊 Playground disponible en ${url}`);
    console.log('');
    console.log('🔐 Sistema de autenticación:');
    console.log('  ✅ Autenticado automáticamente como admin');
    console.log('  ✅ Token JWT configurado en todas las peticiones');
    console.log('  ✅ Renovación automática de token activa');
    console.log('');
    console.log('🌐 Microservicios (vía API Gateway):');
    console.log(`  - Auth Service:     ${config.authServiceUrl}`);
    console.log(`  - Pedido Service:   ${config.pedidoServiceUrl}`);
    console.log(`  - Fleet Service:    ${config.fleetServiceUrl}`);
    console.log(`  - Tracking Service: ${config.trackingServiceUrl}`);
    console.log('');
    console.log('⚡ Optimizaciones activas:');
    console.log('✅ DataLoaders activos (prevención N+1)');
    console.log('✅ Caché en memoria activo con métricas');
    console.log('✅ Interceptors HTTP con manejo automático de errores 401/403');
    
  } catch (error) {
    console.error('❌ Error al inicializar el sistema:', error);
    throw error;
  }
}

// Manejo de cierre del proceso
process.on('SIGINT', () => {
  console.log('\n🔄 Cerrando servidor...');
  authManager.shutdown();
  console.log('👋 Servidor cerrado correctamente');
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('\n🔄 Cerrando servidor...');
  authManager.shutdown();
  console.log('👋 Servidor cerrado correctamente');
  process.exit(0);
});

startServer().catch((error) => {
  console.error('❌ Error al iniciar el servidor:', error);
  authManager.shutdown();
  process.exit(1);
});
