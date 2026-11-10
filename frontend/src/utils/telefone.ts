export const MENSAGEM_TELEFONE = 'Informe um telefone válido com DDD, por exemplo (85) 99999-9999.'

export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '')
}

// "85999990000" -> "(85) 99999-0000"; aceita digitação parcial (máscara enquanto digita).
export function formatarTelefone(valor: string): string {
  const d = somenteDigitos(valor).slice(0, 11)
  if (d.length === 0) return ''
  if (d.length <= 2) return `(${d}`
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`
}

// Mesma regra do backend: DDD + número, 10 dígitos (fixo) ou 11 (celular, começando em 9).
export function telefoneValido(valor: string): boolean {
  const d = somenteDigitos(valor)
  return (d.length === 10 || d.length === 11) && Number(d.slice(0, 2)) >= 11 && (d.length === 10 || d[2] === '9')
}
