export const REGIOES = [
  'Aldeota', 'Benfica', 'Centro', 'Cocó', 'Dionísio Torres', 'Edson Queiroz', 'Fátima',
  'Joaquim Távora', 'Meireles', 'Messejana', 'Montese', 'Papicu', 'Parangaba', 'Praia de Iracema',
  'Varjota', 'Vila União',
]

export function rotuloRegiao(regiao: string): string {
  return `${regiao} - Fortaleza, CE`
}
