import { api } from './client'

export interface NovoUsuario {
  nome: string
  email: string
  senha: string
  regiao: string
  aceitouTermos: boolean
}

export const authApi = {
  cadastrar: (dados: NovoUsuario) =>
    api<unknown>('/usuarios', { method: 'POST', body: JSON.stringify(dados) }),
  esqueciSenha: (email: string) =>
    api<{ mensagem: string }>('/auth/esqueci-senha', { method: 'POST', body: JSON.stringify({ email }) }),
  redefinirSenha: (token: string, novaSenha: string) =>
    api<null>('/auth/redefinir-senha', { method: 'POST', body: JSON.stringify({ token, novaSenha }) }),
}

export const SENHA_MIN = 8

export const REGIOES = [
  'Aldeota', 'Benfica', 'Centro', 'Cocó', 'Dionísio Torres', 'Edson Queiroz', 'Fátima',
  'Joaquim Távora', 'Meireles', 'Messejana', 'Montese', 'Papicu', 'Parangaba', 'Praia de Iracema',
  'Varjota', 'Vila União',
]
