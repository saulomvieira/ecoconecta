import { useState } from 'react'
import { CheckCircle2, Eye, Search, Sprout, Trash2 } from 'lucide-react'
import { adminApi, formatarData, mensagemErro, type Residuo } from '../api/admin'
import { Badge } from '../components/Badge'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useCarregar } from '../components/useCarregar'

const semAcento = (s: string) => s.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()

export function ResiduosPage() {
  const { dados, erro, carregando, recarregar } = useCarregar(async () => {
    const [residuos, usuarios] = await Promise.all([adminApi.residuos(), adminApi.usuarios()])
    return { residuos, geradores: new Map(usuarios.map((u) => [u.id, u.nome])) }
  })
  const [busca, setBusca] = useState('')
  const [categoria, setCategoria] = useState('')
  const [status, setStatus] = useState('')
  const [detalhe, setDetalhe] = useState<Residuo | null>(null)
  const [excluir, setExcluir] = useState<Residuo | null>(null)
  const [excluindo, setExcluindo] = useState(false)
  const [erroAcao, setErroAcao] = useState<string | null>(null)

  const residuos = dados?.residuos ?? []
  const gerador = (r: Residuo) => dados?.geradores.get(r.usuarioId) ?? '—'
  const categorias = [...new Set(residuos.map((r) => r.categoria))].sort()
  const termo = semAcento(busca.trim())
  const filtrados = residuos.filter(
      (r) => (!termo || semAcento(r.nome).includes(termo)) && (!categoria || r.categoria === categoria) && (!status || r.status === status),
  )

  async function confirmarExclusao() {
    if (!excluir) return
    setExcluindo(true)
    setErroAcao(null)
    try {
      await adminApi.excluirResiduo(excluir.id)
      setExcluir(null)
      await recarregar()
    } catch (e) {
      setErroAcao(mensagemErro(e, 'Falha ao excluir.'))
    } finally {
      setExcluindo(false)
    }
  }

  return (
    <>
      <PageHeader titulo="Gerenciamento de Resíduos" />
      <div className="filters">
        <label className="search">
          <Search />
          <input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar resíduos…" />
        </label>
        <select className="select" value={categoria} onChange={(e) => setCategoria(e.target.value)} aria-label="Filtrar por classificação">
          <option value="">Classificação: Todos</option>
          {categorias.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <select className="select" value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Filtrar por status">
          <option value="">Status: Todos</option>
          <option value="DISPONIVEL">Disponível</option>
          <option value="RESERVADO">Reservado</option>
          <option value="COLETADO">Coletado</option>
        </select>
      </div>

      <div className="stat-grid cols-3">
        <StatCard icon={Trash2} valor={residuos.length} rotulo="Resíduos Cadastrados" />
        <StatCard icon={CheckCircle2} valor={residuos.filter((r) => r.status === 'DISPONIVEL').length} rotulo="Disponíveis para Coleta" />
        <StatCard icon={Sprout} valor={residuos.filter((r) => r.status === 'COLETADO').length} rotulo="Já Coletados" />
      </div>

      {erro && <p className="error" role="alert">{erro}</p>}

      <div className="card table-card">
        <table className="table">
          <thead>
            <tr><th>Descrição</th><th>Gerador</th><th>Região</th><th>Classificação</th><th>Status</th><th>Data</th><th className="center">Ações</th></tr>
          </thead>
          <tbody>
            {carregando && <tr><td colSpan={7} className="muted">Carregando…</td></tr>}
            {!carregando && filtrados.length === 0 && <tr><td colSpan={7} className="muted">Nenhum resíduo encontrado.</td></tr>}
            {filtrados.map((r) => (
              <tr key={r.id}>
                <td className="strong">{r.nome} ({r.quantidade} {r.unidadeMedida})</td>
                <td>{gerador(r)}</td>
                <td>{r.regiao}</td>
                <td>{r.categoria}</td>
                <td><Badge valor={r.status} /></td>
                <td>{formatarData(r.dataCadastro)}</td>
                <td className="center">
                  <button type="button" className="icon-btn" onClick={() => setDetalhe(r)} aria-label={`Ver ${r.nome}`} title="Ver detalhes"><Eye /></button>
                  <button type="button" className="icon-btn danger" onClick={() => { setErroAcao(null); setExcluir(r) }} aria-label={`Excluir ${r.nome}`} title="Excluir"><Trash2 /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="table-count muted">
        Exibindo <strong>{filtrados.length}</strong> de <strong>{residuos.length}</strong> registros
      </p>

      {detalhe && (
        <Modal titulo={detalhe.nome} onClose={() => setDetalhe(null)}>
          <dl className="detail">
            <dt>Descrição</dt><dd>{detalhe.descricao}</dd>
            <dt>Gerador</dt><dd>{gerador(detalhe)}</dd>
            <dt>Classificação</dt><dd>{detalhe.categoria}</dd>
            <dt>Quantidade</dt><dd>{detalhe.quantidade} {detalhe.unidadeMedida}</dd>
            <dt>Região</dt><dd>{detalhe.regiao}</dd>
            <dt>Endereço de coleta</dt><dd>{detalhe.enderecoColeta}</dd>
            <dt>Status</dt><dd><Badge valor={detalhe.status} /></dd>
            <dt>Cadastrado em</dt><dd>{formatarData(detalhe.dataCadastro)}</dd>
          </dl>
          <div className="actions end"><button type="button" className="btn" onClick={() => setDetalhe(null)}>Fechar</button></div>
        </Modal>
      )}

      {excluir && (
        <Modal titulo="Excluir resíduo" onClose={() => setExcluir(null)}>
          <p>Excluir “{excluir.nome}”? Esta ação não pode ser desfeita.</p>
          {erroAcao && <p className="error" role="alert">{erroAcao}</p>}
          <div className="actions end">
            <button type="button" className="btn" onClick={() => setExcluir(null)}>Cancelar</button>
            <button type="button" className="btn danger" onClick={confirmarExclusao} disabled={excluindo}>
              {excluindo ? 'Excluindo…' : 'Excluir'}
            </button>
          </div>
        </Modal>
      )}
    </>
  )
}
