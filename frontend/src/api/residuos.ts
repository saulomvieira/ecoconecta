
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
