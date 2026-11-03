
import { api } from './client'

export interface Residuo {
  id: number
  nome: string
  descricao: string
  categoria: string
  quantidade: number
  unidadeMedida: string
  regiao: string
  enderecoColeta: string
  imagemUrl: string | null
  dataCadastro: string
  usuario?: { id: number; nome: string }
}

export interface ResiduoDisponibilidade {
  residuo: Residuo
  status: string
}

export function listarFeed(
  regiao?: string,
  priorizarRegiao = false,
): Promise<ResiduoDisponibilidade[]> {
  const params = new URLSearchParams()

  if (regiao?.trim()) {
    params.set('regiao', regiao.trim())
  }

  params.set('priorizarRegiao', String(priorizarRegiao))

  return api<ResiduoDisponibilidade[]>(
    `/residuos/feed?${params.toString()}`
  )
}

export interface NovoResiduo {
  nome: string
  descricao: string
  categoria: string
  quantidade: number
  unidadeMedida: string
  regiao: string
  enderecoColeta: string
}

export function cadastrarResiduo(dados: NovoResiduo): Promise<Residuo> {
  return api<Residuo>('/residuos', { method: 'POST', body: JSON.stringify(dados) })
}

export function enviarImagemResiduo(id: number, arquivo: File): Promise<{ imagemUrl: string }> {
  const corpo = new FormData()
  corpo.append('arquivo', arquivo)
  return api<{ imagemUrl: string }>(`/residuos/${id}/imagem`, { method: 'POST', body: corpo })
}

export function atualizarResiduo(id: number, dados: NovoResiduo & { imagemUrl: string | null }): Promise<Residuo> {
  return api<Residuo>(`/residuos/${id}`, { method: 'PUT', body: JSON.stringify(dados) })
}

export function excluirResiduo(id: number): Promise<void> {
  return api<void>(`/residuos/${id}`, { method: 'DELETE' })
}
