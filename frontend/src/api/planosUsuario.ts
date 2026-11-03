import { api } from './client'

export interface PlanoUsuario {
  id: number
  nome: string
  preco: number
  limiteColetasMes: number | null
  limiteColetasSemana: number | null
  beneficios: string
  ativo: boolean
}

export type SituacaoAssinatura = 'ATIVA' | 'INADIMPLENTE' | 'CANCELADA' | 'EXPIRADA'

export interface MinhaAssinatura {
  id: number
  planoId: number
  planoNome: string
  status: SituacaoAssinatura
  dataInicio: string
  dataFim: string | null
}

export function consultarPlanos(): Promise<PlanoUsuario[]> {
  return api<PlanoUsuario[]>('/planos')
}

export function consultarMinhasAssinaturas(): Promise<MinhaAssinatura[]> {
  return api<MinhaAssinatura[]>('/assinaturas/minha')
}

export function assinarPlano(usuarioId: number, planoId: number): Promise<unknown> {
  return api('/assinaturas', {
    method: 'POST',
    body: JSON.stringify({ usuario: { id: usuarioId }, plano: { id: planoId } }),
  })
}

export function cancelarMinhaAssinatura(assinaturaId: number): Promise<unknown> {
  return api(`/assinaturas/${assinaturaId}/cancelar`, { method: 'PATCH' })
}
