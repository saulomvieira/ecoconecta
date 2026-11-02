import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../api/client'
import { proximaVersao, termosApi, type TermoUso } from '../api/termos'
import { PageHeader } from '../components/PageHeader'
import { RichTextEditor } from '../components/RichTextEditor'

const formatarData = (iso: string) => new Date(iso).toLocaleDateString('pt-BR')
const formatarDataHora = (iso: string) => new Date(iso).toLocaleString('pt-BR')

export function TermosUsoPage() {
  const [atual, setAtual] = useState<TermoUso | null>(null)
  const [conteudo, setConteudo] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)
  const [historico, setHistorico] = useState<TermoUso[] | null>(null)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      const termo = await termosApi.atual()
      setAtual(termo)
      setConteudo(termo.conteudo)
    } catch (e) {
      // 404 = ainda não há termo publicado: o admin cria a versão 1.0.
      if (!(e instanceof ApiError && e.status === 404)) {
        setErro(e instanceof ApiError ? e.message : 'Falha ao carregar os termos.')
      }
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    carregar()
  }, [carregar])

  const alterado = conteudo !== (atual?.conteudo ?? '')
  const proxima = proximaVersao(atual?.versao ?? null)

  async function salvar() {
    setErro(null)
    setSucesso(null)
    setSalvando(true)
    try {
      const novo = await termosApi.publicar(conteudo)
      setAtual(novo)
      setConteudo(novo.conteudo)
      setSucesso(`Versão v${novo.versao} publicada.`)
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Falha ao salvar.')
    } finally {
      setSalvando(false)
    }
  }

  async function abrirHistorico() {
    setErro(null)
    try {
      setHistorico(await termosApi.historico())
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Falha ao carregar o histórico.')
    }
  }

  return (
    <>
      <PageHeader titulo="Termos de Uso" />

      <section className="card version-card">
        <div>
          <h2>Versão Atual dos Termos</h2>
          <p className="muted">
            {atual
              ? `Última atualização registrada: ${formatarData(atual.dataAtualizacao)} por ${atual.atualizadoPor.nome}`
              : 'Nenhuma versão publicada ainda.'}
          </p>
        </div>
        {atual && <span className="pill">v{atual.versao} Ativo</span>}
      </section>

      <section className="card editor-card">
        {carregando ? (
          <p className="muted">Carregando…</p>
        ) : (
          <RichTextEditor value={conteudo} onChange={setConteudo} disabled={salvando} />
        )}
      </section>

      {erro && <p className="error" role="alert">{erro}</p>}
      {sucesso && <p className="success" role="status">{sucesso}</p>}

      <footer className="page-footer">
        <p className="muted small">
          * Ao salvar, uma nova versão será criada automaticamente com acréscimo incremental (v{proxima}).
        </p>
        <div className="actions">
          <button type="button" className="btn" onClick={abrirHistorico}>
            Histórico de Versões
          </button>
          <button
            type="button"
            className="btn primary"
            onClick={salvar}
            disabled={salvando || carregando || !conteudo || !alterado}
          >
            {salvando ? 'Salvando…' : 'Salvar Alterações'}
          </button>
        </div>
      </footer>

      {historico && (
        <div className="modal-backdrop" onClick={() => setHistorico(null)}>
          <div
            className="card modal"
            role="dialog"
            aria-modal="true"
            aria-label="Histórico de versões"
            onClick={(e) => e.stopPropagation()}
          >
            <h2>Histórico de Versões</h2>
            <ul className="history">
              {historico.map((t) => (
                <li key={t.id}>
                  <button
                    type="button"
                    className="history-item"
                    onClick={() => {
                      setConteudo(t.conteudo)
                      setHistorico(null)
                    }}
                    title="Carregar no editor (não publica até salvar)"
                  >
                    <strong>v{t.versao}</strong>
                    {t.ativo && <span className="pill">Ativo</span>}
                    <span className="muted small">
                      {formatarDataHora(t.dataAtualizacao)} · {t.atualizadoPor.nome}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
            <button type="button" className="btn" onClick={() => setHistorico(null)}>
              Fechar
            </button>
          </div>
        </div>
      )}
    </>
  )
}
