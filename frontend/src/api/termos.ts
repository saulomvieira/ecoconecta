import { api } from './client'

export interface TermoUso {
  id: number
  versao: string
  conteudo: string
  ativo: boolean
  dataAtualizacao: string
  atualizadoPor: { id: number; nome: string }
}

export const termosApi = {
  atual: () => api<TermoUso>('/termos/atual'),
  historico: () => api<TermoUso[]>('/termos'),
  publicar: (conteudo: string) =>
    api<TermoUso>('/termos', {
      method: 'POST',
      body: JSON.stringify({ conteudo }),
    }),
}

// "1.2" -> "1.3" (mesma regra do backend)
export function proximaVersao(versao: string | null): string {
  if (!versao) return '1.0'
  const [maior, menor] = versao.split('.')
  return `${maior}.${Number(menor) + 1}`
}
