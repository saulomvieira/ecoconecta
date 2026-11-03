import type { Residuo } from '../api/residuos'

export const STATUS_RESIDUO: Record<string, { rotulo: string; classe: string }> = {
  DISPONIVEL: { rotulo: 'Disponível', classe: 'disponivel' },
  RESERVADO: { rotulo: 'Reservado', classe: 'reservado' },
  COLETADO: { rotulo: 'Coletado', classe: 'coletado' },
}

export function urlImagem(residuo: Residuo): string | null {
  if (!residuo.imagemUrl) return null
  return residuo.imagemUrl.startsWith('/residuos/') ? `/api${residuo.imagemUrl}` : residuo.imagemUrl
}
