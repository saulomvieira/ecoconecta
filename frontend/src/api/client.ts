const TOKEN_KEY = 'ecoconecta.token'

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

// Disparado quando o backend responde 401 com token presente (sessão expirada).
export const UNAUTHORIZED_EVENT = 'ecoconecta:unauthorized'

// O backend devolve { message } ou { campo: mensagem } nos erros de validação.
function extrairMensagem(corpo: unknown, status: number): string {
  if (corpo && typeof corpo === 'object') {
    const valores = Object.values(corpo as Record<string, unknown>).filter(
      (v): v is string => typeof v === 'string',
    )
    if (valores.length) return valores.join(' ')
  }
  if (status === 403) return 'Você não tem permissão para esta ação.'
  return 'Não foi possível concluir a operação.'
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = tokenStorage.get()
  const headers = new Headers(init.headers)
  if (init.body) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  let resposta: Response
  try {
    resposta = await fetch(`/api${path}`, { ...init, headers })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor.')
  }

  const texto = await resposta.text()
  const corpo = texto ? JSON.parse(texto) : null

  if (!resposta.ok) {
    if (resposta.status === 401 && token) {
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    }
    throw new ApiError(resposta.status, extrairMensagem(corpo, resposta.status))
  }
  return corpo as T
}
