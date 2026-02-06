import { GraphQLContext } from '../../index';
import { 
  Usuario, 
  RepartidorDetalle, 
  Pedido, 
  Incidencia,
  ActualizarDatosContactoInput,
  RegistrarIncidenciaInput
} from '../../entities';
import { EstadoRepartidor } from '../../enums';

// ✅ Agregamos 'export' para resolver el error TS4023
export interface ActualizarEstadoRepartidorArgs {
  input: {
    repartidorId: string;
    estado: EstadoRepartidor;
    motivo?: string;
  };
}

export interface ReasignarPedidoArgs {
  input: {
    pedidoId: string;
    nuevoRepartidorId: string;
    motivo?: string;
  };
}

export interface ActualizarDatosContactoArgs {
  input: ActualizarDatosContactoInput;
}

export interface RegistrarIncidenciaArgs {
  input: RegistrarIncidenciaInput;
}

/**
 * Resolvers para las MUTATIONS de GraphQL
 */
export const mutationResolvers = {
  actualizarEstadoRepartidor: async (
    parent: any,
    args: ActualizarEstadoRepartidorArgs,
    context: GraphQLContext
  ): Promise<RepartidorDetalle> => {
    const { repartidorId, estado, motivo } = args.input;
    console.log(`[Mutation] Actualizando repartidor ${repartidorId} a ${estado}`);
    
    try {
      // ✅ Este método debe existir en fleetClient para evitar el error TS2339
      return await context.fleetClient.actualizarEstadoRepartidor(repartidorId, estado, motivo);
    } catch (error) {
      throw new Error(`Error en fleet-service: ${error}`);
    }
  },

  reasignarPedido: async (
    parent: any,
    args: ReasignarPedidoArgs,
    context: GraphQLContext
  ): Promise<Pedido> => {
    const { pedidoId, nuevoRepartidorId, motivo } = args.input;
    try {
      // ✅ Herramienta de reasignación manual requerida para la Fase 3
      return await context.pedidoService.reasignarPedido(pedidoId, nuevoRepartidorId, motivo);
    } catch (error) {
      throw new Error(`Error al reasignar: ${error}`);
    }
  },

  actualizarDatosContacto: async (
    parent: any,
    args: ActualizarDatosContactoArgs,
    context: GraphQLContext
  ): Promise<Usuario> => {
    try {
      return await context.authClient.actualizarDatosContacto(args.input);
    } catch (error) {
      throw new Error(`Error en auth-service: ${error}`);
    }
  },

  registrarIncidencia: async (
    parent: any,
    args: RegistrarIncidenciaArgs,
    context: GraphQLContext
  ): Promise<Incidencia> => {
    try {
      // ✅ Reporte de incidencias solicitado en el panel de control
      return await context.incidenciaClient.registrarIncidencia(args.input);
    } catch (error) {
      throw new Error(`Error al registrar incidencia: ${error}`);
    }
  }
};