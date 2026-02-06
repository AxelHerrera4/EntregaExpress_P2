import { authClient } from '../utils';
import { Usuario, ActualizarDatosContactoInput } from '../entities';

/**
 * AuthServiceClient - Comunicación con el microservicio de Autenticación (puerto 8081)
 */
export class AuthServiceClient {
  /**
   * Actualiza los datos de contacto de un usuario
   * PATCH /api/usuarios/{usuarioId}/contacto
   */
  async actualizarDatosContacto(input: ActualizarDatosContactoInput): Promise<Usuario> {
    try {
      const payload = {
        telefono: input.telefono,
        email: input.email,
        nombre: input.nombre
      };
      
      const response = await authClient.patch<Usuario>(
        `/api/usuarios/${input.usuarioId}/contacto`,
        payload
      );
      
      console.log(`[AuthServiceClient] Datos de contacto actualizados para usuario ${input.usuarioId}`);
      return response.data;
    } catch (error) {
      console.error(`[AuthServiceClient] Error al actualizar datos de contacto del usuario ${input.usuarioId}:`, error);
      throw new Error(`Error al actualizar datos de contacto: ${error}`);
    }
  }

  /**
   * Obtiene información de un usuario específico
   * GET /api/usuarios/{usuarioId}
   */
  async obtenerUsuario(usuarioId: string): Promise<Usuario | null> {
    try {
      const response = await authClient.get<Usuario>(`/api/usuarios/${usuarioId}`);
      return response.data;
    } catch (error) {
      console.error(`[AuthServiceClient] Error al obtener usuario ${usuarioId}:`, error);
      return null;
    }
  }
}