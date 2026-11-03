function plural(n: number, singular: string, plural: string) {
  return `há ${n} ${n === 1 ? singular : plural}`
}

export function tempoDecorrido(iso: string): string {
  const minutos = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 60000))
  if (minutos < 1) return 'agora há pouco'
  if (minutos < 60) return plural(minutos, 'minuto', 'minutos')
  const horas = Math.floor(minutos / 60)
  if (horas < 24) return plural(horas, 'hora', 'horas')
  const dias = Math.floor(horas / 24)
  if (dias < 30) return plural(dias, 'dia', 'dias')
  const meses = Math.floor(dias / 30)
  if (meses < 12) return plural(meses, 'mês', 'meses')
  return plural(Math.floor(meses / 12), 'ano', 'anos')
}

export function semAcento(texto: string): string {
  return texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase().trim()
}
