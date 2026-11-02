
import type { ResiduoDisponibilidade } from '../api/residuos'

interface Props {
  item: ResiduoDisponibilidade
}

export function ResiduoCard({ item }: Props) {
  const { residuo, status } = item

  return (
    <article className="residuo-card">
      {residuo.imagemUrl && (
        <img
          src={residuo.imagemUrl}
          alt={residuo.nome}
          className="residuo-imagem"
        />
      )}

      <div className="residuo-conteudo">
        <span className="residuo-categoria">
          {residuo.categoria}
        </span>

        <h3>{residuo.nome}</h3>

        <p>{residuo.descricao}</p>

        <p>
          <strong>Quantidade:</strong>{' '}
          {residuo.quantidade} {residuo.unidadeMedida}
        </p>

        <p>
          <strong>Região:</strong> {residuo.regiao}
        </p>

        <span className="residuo-status">
          {status}
        </span>
      </div>
    </article>
  )
}
