const mapa: Record<string, [string, string]> = {
  DISPONIVEL: ['Disponível', 'ok'], RESERVADO: ['Reservado', 'warn'], COLETADO: ['Coletado', 'off'],
  ATIVA: ['Ativa', 'ok'], INADIMPLENTE: ['Inadimplente', 'bad'], CANCELADA: ['Cancelada', 'off'], EXPIRADA: ['Expirada', 'off'],
  ATIVO: ['Ativo', 'ok'], INATIVO: ['Inativo', 'off'],
}

export function Badge({ valor }: { valor: string }) {
  const [texto, tom] = mapa[valor] ?? [valor, 'off']
  return <span className={`badge ${tom}`}>{texto}</span>
}
