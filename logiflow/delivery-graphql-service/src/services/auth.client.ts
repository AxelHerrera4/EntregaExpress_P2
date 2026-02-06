import { authClient } from '../utils';
import { Usuario, ActualizarDatosContactoInput } from '../entities';

/**
 * AuthServiceClient - Comunicación con el microservicio de Autenticación a través del API Gateway
 */
export class AuthServiceClient {
  /**
   * Actualiza los datos de contacto de un usuario
   * PATCH /usuarios/{usuarioId}/contacto (se envía a /api/auth/usuarios/{usuarioId}/contacto via API Gateway)
   */
  async actualizarDatosContacto(input: ActualizarDatosContactoInput): Promise<Usuario> {
    try {
      const payload = {
        telefono: input.telefono,
        email: input.email,
        nombre: input.nombre
      };
      
      const response = await authClient.patch<Usuario>(
        `/usuarios/${input.usuarioId}/contacto`,
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
   * GET /usuarios/{usuarioId} (se envía a /api/auth/usuarios/{usuarioId} via API Gateway)
   */
  async obtenerUsuario(usuarioId: string): Promise<Usuario | null> {
    try {
      const response = await authClient.get<Usuario>(`/usuarios/${usuarioId}`);
      return response.data;
    } catch (error) {
      console.error(`[AuthServiceClient] Error al obtener usuario ${usuarioId}:`, error);
      return null;
    }
  }
}