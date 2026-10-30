import { api } from './client'

export interface MeuPerfil {
  id: number
  nome: string
  email: string
}

export const perfilApi = {
  meu: () => api<MeuPerfil>('/usuarios/me'),
}

// "Bruno Lourenço Santos" -> "Bruno L."
export function nomeAbreviado(nome: string): string {
  const [primeiro, ...resto] = nome.trim().split(/\s+/)
  return resto.length ? `${primeiro} ${resto[0][0].toUpperCase()}.` : primeiro
}

// "Bruno Lourenço Santos" -> "BL"
export function iniciais(nome: string): string {
  const [primeiro, ...resto] = nome.trim().split(/\s+/)
  return (primeiro[0] + (resto[0]?.[0] ?? '')).toUpperCase()
}
