
import { useEffect, useMemo, useState } from 'react'
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

  const [busca, setBusca] = useState('')
  const [categoria, setCategoria] = useState('')
  const [regiao, setRegiao] = useState('')
  const [somenteDisponiveis, setSomenteDisponiveis] = useState(false)

  useEffect(() => {
    let ativo = true

    async function carregarResiduos() {
      try {
        const dados = await listarFeed()
        if (ativo) setResiduos(dados)
      } catch {
        if (ativo) {
          setErro('Não foi possível carregar os resíduos.')
        }
      } finally {
        if (ativo) setCarregando(false)
      }
    }

    carregarResiduos()

    return () => {
      ativo = false
    }
  }, [])

  const categorias = useMemo(
    () =>
      [...new Set(residuos.map((item) => item.residuo.categoria))]
        .filter(Boolean)
        .sort(),
    [residuos]
  )

  const regioes = useMemo(
    () =>
      [...new Set(residuos.map((item) => item.residuo.regiao))]
        .filter(Boolean)
        .sort(),
    [residuos]
  )

  const residuosFiltrados = useMemo(() => {
    const buscaNormalizada = busca.trim().toLocaleLowerCase('pt-BR')

    return residuos.filter(({ residuo, status }) => {
      const correspondeBusca =
        residuo.nome.toLocaleLowerCase('pt-BR')
          .includes(buscaNormalizada)

      const correspondeCategoria =
        !categoria || residuo.categoria === categoria

      const correspondeRegiao =
        !regiao || residuo.regiao === regiao

      const correspondeDisponibilidade =
        !somenteDisponiveis || status === 'DISPONIVEL'

      return (
        correspondeBusca &&
        correspondeCategoria &&
        correspondeRegiao &&
        correspondeDisponibilidade
      )
    })
  }, [residuos, busca, categoria, regiao, somenteDisponiveis])

  function limparFiltros() {
    setBusca('')
    setCategoria('')
    setRegiao('')
    setSomenteDisponiveis(false)
  }

  return (
    <main className="feed-container">
      <header className="feed-header">
        <h1>Explorar resíduos</h1>
        <p>
          Encontre materiais disponíveis para reutilização,
          reciclagem e compostagem.
        </p>
      </header>

      <section className="feed-filtros" aria-label="Filtros de resíduos">
        <input
          type="search"
          placeholder="Pesquisar resíduos..."
          aria-label="Pesquisar por nome"
          value={busca}
          onChange={(event) => setBusca(event.target.value)}
        />

        <select
          aria-label="Filtrar por categoria"
          value={categoria}
          onChange={(event) => setCategoria(event.target.value)}
        >
          <option value="">Todas as categorias</option>
          {categorias.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>

        <select
          aria-label="Filtrar por região"
          value={regiao}
          onChange={(event) => setRegiao(event.target.value)}
        >
          <option value="">Todas as regiões</option>
          {regioes.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>

        <label className="feed-checkbox">
          <input
            type="checkbox"
            checked={somenteDisponiveis}
            onChange={(event) =>
              setSomenteDisponiveis(event.target.checked)
            }
          />
          Somente disponíveis
        </label>

        <button type="button" onClick={limparFiltros}>
          Limpar filtros
        </button>
      </section>

      {carregando && <p>Carregando resíduos...</p>}

      {erro && <p role="alert">{erro}</p>}

      {!carregando && !erro && (
        <>
          <p className="feed-resultados">
            {residuosFiltrados.length} resíduos encontrados
          </p>

          {residuosFiltrados.length === 0 && (
            <p>Nenhum resíduo encontrado para os filtros selecionados.</p>
          )}

          <section className="feed-grid">
            {residuosFiltrados.map((item) => (
              <ResiduoCard
                key={item.residuo.id}
                item={item}
              />
            ))}
          </section>
        </>
      )}
    </main>
  )
}
