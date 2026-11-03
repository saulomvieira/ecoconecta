import { ArrowRight, TriangleAlert } from 'lucide-react'

export type TipoLimite = 'MENSAL' | 'SEMANAL'

interface Props {
  tipo: TipoLimite
  plano: string
  limite: number | null
  usadas: number | null
  onVoltar: () => void
  onVerPlanos: () => void
}

export function LimiteColetas({ tipo, plano, limite, usadas, onVoltar, onVerPlanos }: Props) {
  const mensal = tipo === 'MENSAL'
  const periodo = mensal ? 'este mês' : 'esta semana'
  const temNumeros = limite != null && usadas != null
  const porcentagem = temNumeros && limite > 0 ? Math.min(100, (usadas / limite) * 100) : 100

  return (
    <div className="limite-conteudo">
      <span className="limite-icone"><TriangleAlert aria-hidden="true" /></span>
      <h1 id="detalhe-titulo">{mensal ? 'Limite mensal atingido' : 'Limite semanal atingido'}</h1>
      <p className="limite-texto">
        {temNumeros
          ? `Você já usou as ${limite} coletas do plano ${plano} ${periodo}. `
          : `Você atingiu o limite de coletas do seu plano ${periodo}. `}
        {mensal
          ? 'Para continuar reservando, assine um plano pago.'
          : 'Aguarde a próxima semana ou assine um plano com mais coletas.'}
      </p>

      <div className="limite-progresso">
        <span className="limite-rotulo">{mensal ? 'Coletas do mês' : 'Coletas da semana'}</span>
        <div
          className="limite-barra"
          role="progressbar"
          aria-valuemin={0}
          aria-valuemax={limite ?? 100}
          aria-valuenow={usadas ?? limite ?? 100}
        >
          <span style={{ width: `${porcentagem}%` }} />
        </div>
        {temNumeros && <strong className="limite-valor">{usadas} / {limite}</strong>}
      </div>

      <div className="limite-acoes">
        <button type="button" className="detalhe-botao secundario" onClick={onVoltar}>Voltar ao feed</button>
        <button type="button" className="detalhe-botao" onClick={onVerPlanos}>
          Ver planos <ArrowRight aria-hidden="true" />
        </button>
      </div>
    </div>
  )
}
