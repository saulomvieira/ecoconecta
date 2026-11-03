import { useEffect, useMemo, useRef, useState } from 'react'
import { ChevronDown, MapPin, Search } from 'lucide-react'
import { perfilApi } from '../api/perfil'
import { REGIOES, rotuloRegiao } from '../api/regioes'
import type { ResiduoDisponibilidade } from '../api/residuos'
import { useAuth } from '../auth/AuthContext'
import { ResiduoCard } from '../components/ResiduoCard'
import { semAcento } from '../utils/tempo'
import './FeedResiduos.css'

const mesmaRegiao = (a: string, b: string) => semAcento(a) === semAcento(b)

interface Props {
  residuos: ResiduoDisponibilidade[]
  carregando: boolean
  erro: string
}

export function FeedResiduos({ residuos, carregando, erro }: Props) {
  const { sessao } = useAuth()

  const [busca, setBusca] = useState('')
  const [categoria, setCategoria] = useState('')
  const [somenteDisponiveis, setSomenteDisponiveis] = useState(true)
  const [regiao, setRegiao] = useState('')
  const [regiaoUsuario, setRegiaoUsuario] = useState('')
  const escolhaManual = useRef(false)

  const logado = sessao != null
  useEffect(() => {
    if (!logado) return
    let ativo = true
    perfilApi
      .meu()
      .then(({ regiao: regiaoDoPerfil }) => {
        if (!ativo || !regiaoDoPerfil) return
        const canonica = REGIOES.find((r) => mesmaRegiao(r, regiaoDoPerfil)) ?? regiaoDoPerfil
        setRegiaoUsuario(canonica)
        if (!escolhaManual.current) setRegiao(canonica)
      })
      .catch(() => {})
    return () => {
      ativo = false
    }
  }, [logado])

  const categorias = useMemo(
    () => [...new Set(residuos.map((item) => item.residuo.categoria))].filter(Boolean).sort(),
    [residuos],
  )

  const outrasRegioes = useMemo(() => {
    const extras = new Map<string, string>()
    const candidatas = [...residuos.map((item) => item.residuo.regiao), regiaoUsuario]
    for (const candidata of candidatas) {
      if (!candidata || REGIOES.some((r) => mesmaRegiao(r, candidata))) continue
      extras.set(semAcento(candidata), candidata)
    }
    return [...extras.values()].sort((a, b) => a.localeCompare(b, 'pt-BR'))
  }, [residuos, regiaoUsuario])

  const filtrados = useMemo(() => {
    const termo = semAcento(busca)
    return residuos.filter(
      ({ residuo, status }) =>
        (!termo || semAcento(residuo.nome).includes(termo)) &&
        (!categoria || residuo.categoria === categoria) &&
        (!somenteDisponiveis || status === 'DISPONIVEL'),
    )
  }, [residuos, busca, categoria, somenteDisponiveis])

  const daRegiao = regiao ? filtrados.filter((i) => mesmaRegiao(i.residuo.regiao, regiao)) : filtrados
  const outras = regiao ? filtrados.filter((i) => !mesmaRegiao(i.residuo.regiao, regiao)) : []
  const filtrosAtivos = busca !== '' || categoria !== '' || !somenteDisponiveis

  function escolherRegiao(valor: string) {
    escolhaManual.current = true
    setRegiao(valor)
  }

  function limparFiltros() {
    setBusca('')
    setCategoria('')
    setSomenteDisponiveis(true)
  }

  const preSelecionada = regiao !== '' && regiaoUsuario !== '' && mesmaRegiao(regiao, regiaoUsuario)

  return (
    <main className="feed-container">
      <header className="feed-topo">
        <h1>Resíduos disponíveis</h1>

        <div className="feed-regiao">
          <label className="feed-regiao-select">
            <MapPin aria-hidden="true" />
            <select
              aria-label="Região"
              value={regiao}
              onChange={(event) => escolherRegiao(event.target.value)}
            >
              <option value="">Todas as regiões</option>
              {REGIOES.map((item) => (
                <option key={item} value={item}>
                  {rotuloRegiao(item)}
                </option>
              ))}
              {outrasRegioes.length > 0 && (
                <optgroup label="Outras">
                  {outrasRegioes.map((item) => (
                    <option key={item} value={item}>
                      {item}
                    </option>
                  ))}
                </optgroup>
              )}
            </select>
            <ChevronDown aria-hidden="true" />
          </label>
          {preSelecionada && (
            <small>Pré-selecionada com a sua região - troque quando quiser</small>
          )}
        </div>
      </header>

      <section className="feed-filtros" aria-label="Filtros de resíduos">
        <label className="search">
          <Search aria-hidden="true" />
          <input
            type="search"
            placeholder="Pesquisar resíduos..."
            aria-label="Pesquisar por nome"
            value={busca}
            onChange={(event) => setBusca(event.target.value)}
          />
        </label>

        <select
          className="select"
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

        <label className="check">
          <input
            type="checkbox"
            checked={somenteDisponiveis}
            onChange={(event) => setSomenteDisponiveis(event.target.checked)}
          />
          Somente disponíveis
        </label>

        {filtrosAtivos && (
          <button type="button" className="feed-limpar" onClick={limparFiltros}>
            Limpar filtros
          </button>
        )}
      </section>

      {carregando && <p className="muted">Carregando resíduos...</p>}
      {erro && <p className="error" role="alert">{erro}</p>}

      {!carregando && !erro && (
        <>
          {filtrados.length === 0 ? (
            <p className="muted">Nenhum resíduo encontrado para os filtros selecionados.</p>
          ) : (
            <>
              {daRegiao.length > 0 ? (
                <section className="feed-grid" aria-label="Resíduos">
                  {daRegiao.map((item) => (
                    <ResiduoCard key={item.residuo.id} item={item} />
                  ))}
                </section>
              ) : (
                <p className="muted">Nenhum resíduo disponível nesta região por enquanto.</p>
              )}

              {outras.length > 0 && (
                <>
                  <div className="feed-divisor" role="separator">
                    <span>Resíduos de outras regiões</span>
                  </div>
                  <section className="feed-grid" aria-label="Resíduos de outras regiões">
                    {outras.map((item) => (
                      <ResiduoCard key={item.residuo.id} item={item} />
                    ))}
                  </section>
                </>
              )}
            </>
          )}
        </>
      )}
    </main>
  )
}
