import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { typeDefs } from './typeDefs/schema';
import { resolvers } from './resolvers';
import {
  PedidoService,
  FleetServiceClient,
  TrackingServiceClient,
  FlotaService,
  KpiService,
} from './services';
import { config } from './utils';

/**
 * Contexto compartido por todos los resolvers
 * Inyecta las instancias de los servicios para que cada resolver pueda usarlos
 */
export interface GraphQLContext {
  pedidoService: PedidoService;
  fleetClient: FleetServiceClient;
  trackingClient: TrackingServiceClient;
  flotaService: FlotaService;
  kpiService: KpiService;
}

// Instanciar servicios (singleton)
const pedidoService = new PedidoService();
const fleetClient = new FleetServiceClient();
const trackingClient = new TrackingServiceClient();
const flotaService = new FlotaService(fleetClient, trackingClient);
const kpiService = new KpiService(pedidoService, fleetClient);

async function startServer(): Promise<void> {
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
    }),
  });

  console.log(`🚀 Servidor GraphQL listo en ${url}`);
  console.log(`📊 Playground disponible en ${url}`);
  console.log('');
  console.log('Microservicios configurados:');
  console.log(`  - Pedido Service:   ${config.pedidoServiceUrl}`);
  console.log(`  - Fleet Service:    ${config.fleetServiceUrl}`);
  console.log(`  - Tracking Service: ${config.trackingServiceUrl}`);
}

startServer().catch((error) => {
  console.error('Error al iniciar el servidor:', error);
  process.exit(1);
});
