
import { useEffect, useState } from 'react'
import {
  listarFeed,
  type ResiduoDisponibilidade,
} from '../api/residuos'
import { ResiduoCard } from '../components/ResiduoCard'
import './FeedResiduos.css'

export function FeedResiduos() {
  const [residuos, setResiduos] = useState<ResiduoDisponibilidade[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true

    async function carregarResiduos() {
      try {
        const dados = await listarFeed()
        if (ativo) setResiduos(dados)
      } catch {
        if (ativo) setErro('Não foi possível carregar os resíduos.')
      } finally {
        if (ativo) setCarregando(false)
      }
    }

    carregarResiduos()

    return () => {
      ativo = false
    }
  }, [])

  return (
    <main className="feed-container">
      <header className="feed-header">
        <h1>Explorar resíduos</h1>
        <p>
          Encontre materiais disponíveis para reutilização,
          reciclagem e compostagem.
        </p>
      </header>

      {carregando && <p>Carregando resíduos...</p>}

      {erro && <p role="alert">{erro}</p>}

      {!carregando && !erro && residuos.length === 0 && (
        <p>Nenhum resíduo disponível no momento.</p>
      )}

      <section className="feed-grid">
        {residuos.map((item) => (
          <ResiduoCard
            key={item.residuo.id}
            item={item}
          />
        ))}
      </section>
    </main>
  )
}
