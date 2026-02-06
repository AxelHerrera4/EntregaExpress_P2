export interface Usuario {
  id: string;
  nombre: string;
  telefono: string;
  email: string;
  rol: string;
}

export interface ActualizarDatosContactoInput {
  usuarioId: string;
  telefono: string;
  email: string;
  nombre?: string;
}