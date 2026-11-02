
import { api } from './client'

export type StatusReserva = 'RESERVADA' | 'COLETADA' | 'CANCELADA'

export interface Reserva {
  id: number
  status: StatusReserva
  dataReserva: string
}

interface NovaReservaRequest {
  usuarioId: number
  residuoId: number
}

export async function solicitarReserva(
  usuarioId: number,
  residuoId: number
): Promise<Reserva> {
  return api<Reserva>('/reservas', {
    method: 'POST',
    body: JSON.stringify({
      usuarioId,
      residuoId,
    } satisfies NovaReservaRequest),
  })
}
