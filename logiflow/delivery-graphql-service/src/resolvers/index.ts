import { queryResolvers } from './queries/query.resolver';
import { pedidoFieldResolvers } from './fields/pedido.resolver';

/**
 * Resolvers combinados para Apollo Server
 */
export const resolvers = {
  Query: queryResolvers,
  Pedido: pedidoFieldResolvers,
};
