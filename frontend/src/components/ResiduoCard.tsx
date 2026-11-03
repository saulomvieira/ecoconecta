import { useState } from 'react'
import { MapPin } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { ResiduoDisponibilidade } from '../api/residuos'
import { STATUS_RESIDUO, urlImagem } from '../utils/residuo'
import { tempoDecorrido } from '../utils/tempo'
import { IconeCategoria } from './IconeCategoria'

interface Props {
  item: ResiduoDisponibilidade
}

export function ResiduoCard({ item }: Props) {
  const { residuo, status } = item
  const [imagemFalhou, setImagemFalhou] = useState(false)
  const situacao = STATUS_RESIDUO[status] ?? { rotulo: status, classe: 'coletado' }
  const disponivel = status === 'DISPONIVEL'
  const detalhes = `/residuos/${residuo.id}`

  const imagemUrl = imagemFalhou ? null : urlImagem(residuo)

  return (
    <article className="feed-card">
      <div className={`feed-card-topo${imagemUrl ? ' com-imagem' : ''}`}>
        {imagemUrl ? (
          <img src={imagemUrl} alt={residuo.nome} onError={() => setImagemFalhou(true)} />
        ) : (
          <IconeCategoria categoria={residuo.categoria} className="feed-card-icone" />
        )}
        <span className="feed-chip categoria">{residuo.categoria}</span>
        <span className={`feed-chip status ${situacao.classe}`}>{situacao.rotulo}</span>
      </div>

      <div className="feed-card-corpo">
        <h3>
          <Link to={detalhes}>{residuo.nome}</Link>
        </h3>

        <p className="feed-card-endereco">
          <MapPin aria-hidden="true" />
          {residuo.enderecoColeta}
        </p>

        <div className="feed-card-rodape">
          <span className="feed-card-tempo">{tempoDecorrido(residuo.dataCadastro)}</span>
          {disponivel ? (
            <Link to={detalhes} className="feed-card-botao">Reservar</Link>
          ) : (
            <button type="button" className="feed-card-botao" disabled>{situacao.rotulo}</button>
          )}
        </div>
      </div>
    </article>
  )
}
