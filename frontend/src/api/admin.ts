import { api } from './client'

export interface Dashboard {
  totalUsuarios: number
  totalResiduos: number
  reservasAtivas: number
  assinaturasAtivas: number
}
export interface ResiduoMensal { mes: string; total: number }
export interface Atividade { tipo: string; descricao: string; data: string }

export type Perfil = 'USUARIO' | 'ADMIN'
export interface Usuario {
  id: number
  nome: string
  email: string
  regiao: string | null
  perfil: Perfil
  ativo: boolean
}

export type StatusResiduo = 'DISPONIVEL' | 'RESERVADO' | 'COLETADO'
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
  usuarioId: number
  status: StatusResiduo
  dataCadastro: string | null
}

export interface Plano {
  id: number
  nome: string
  preco: number
  limiteColetasMes: number | null
  limiteColetasSemana: number | null
  beneficios: string
  ativo: boolean
}
export type PlanoForm = Omit<Plano, 'id'>

export type StatusAssinatura = 'ATIVA' | 'INADIMPLENTE' | 'CANCELADA' | 'EXPIRADA'
export interface Assinatura {
  id: number
  usuarioId: number
  usuarioNome: string
  usuarioEmail: string
  planoId: number
  planoNome: string
  status: StatusAssinatura
  dataInicio: string | null
  dataFim: string | null
}

export const adminApi = {
  dashboard: () => api<Dashboard>('/admin/dashboard'),
  residuosMensais: () => api<ResiduoMensal[]>('/admin/dashboard/residuos-mensais'),
  atividades: () => api<Atividade[]>('/admin/dashboard/atividades-recentes'),

  usuarios: () => api<Usuario[]>('/admin/usuarios'),
  alterarPerfil: (id: number, perfil: Perfil) =>
    api<Usuario>(`/admin/usuarios/${id}/perfil`, { method: 'PATCH', body: JSON.stringify({ perfil }) }),
  alterarAtivo: (id: number, ativo: boolean) =>
    api<Usuario>(`/admin/usuarios/${id}/ativo`, { method: 'PATCH', body: JSON.stringify({ ativo }) }),

  residuos: () => api<Residuo[]>('/admin/residuos'),
  excluirResiduo: (id: number) => api<null>(`/admin/residuos/${id}`, { method: 'DELETE' }),

  planos: () => api<Plano[]>('/planos'),
  criarPlano: (p: PlanoForm) => api<Plano>('/planos', { method: 'POST', body: JSON.stringify(p) }),
  atualizarPlano: (id: number, p: PlanoForm) =>
    api<Plano>(`/planos/${id}`, { method: 'PUT', body: JSON.stringify(p) }),
  assinaturas: () => api<Assinatura[]>('/admin/assinaturas'),
  cancelarAssinatura: (id: number) => api<Assinatura>(`/admin/assinaturas/${id}/cancelar`, { method: 'PATCH' }),
  renovarAssinatura: (id: number) => api<Assinatura>(`/admin/assinaturas/${id}/renovar`, { method: 'PATCH' }),
}

export const formatarData = (iso: string | null) => (iso ? new Date(iso).toLocaleDateString('pt-BR') : '—')
export const formatarMoeda = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

export function tempoRelativo(iso: string): string {
  const min = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 60000))
  if (min < 60) return `há ${Math.max(min, 1)}min`
  if (min < 1440) return `há ${Math.floor(min / 60)}h`
  return `há ${Math.floor(min / 1440)}d`
}

export function mensagemErro(e: unknown, padrao: string): string {
  return e instanceof Error && e.message ? e.message : padrao
}
