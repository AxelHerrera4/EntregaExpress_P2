import { queryResolvers } from './queries/query.resolver';
import { mutationResolvers } from './mutations/mutation.resolver';
import { pedidoFieldResolvers } from './fields/pedido.resolver';

/**
 * Resolvers combinados para Apollo Server
 */
export const resolvers = {
  Query: queryResolvers,
  Mutation: mutationResolvers,
  Pedido: pedidoFieldResolvers,
};
