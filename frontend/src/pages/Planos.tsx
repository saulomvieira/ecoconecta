import { useState } from 'react'
import { CircleCheck, Plus } from 'lucide-react'
import {
  adminApi, formatarData, formatarMoeda, mensagemErro, type Assinatura, type Plano, type PlanoForm,
} from '../api/admin'
import { Badge } from '../components/Badge'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { useCarregar } from '../components/useCarregar'

const vazio: PlanoForm = { nome: '', preco: 0, limiteColetasMes: null, limiteColetasSemana: null, beneficios: '', ativo: true }

function destaques(p: Plano): string[] {
  const itens: string[] = []
  itens.push(p.limiteColetasMes == null ? 'Coletas ilimitadas' : `${p.limiteColetasMes} coletas / mês`)
  if (p.limiteColetasSemana != null) itens.push(`Máximo ${p.limiteColetasSemana} coleta / semana`)
  return [...itens, ...p.beneficios.split('\n').map((l) => l.trim()).filter(Boolean)]
}

export function PlanosPage() {
  const planos = useCarregar(adminApi.planos)
  const assinaturas = useCarregar(adminApi.assinaturas)
  const [form, setForm] = useState<{ id: number | null; dados: PlanoForm } | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [erroModal, setErroModal] = useState<string | null>(null)
  const [erroAcao, setErroAcao] = useState<string | null>(null)
  const [filtroStatus, setFiltroStatus] = useState('')

  const lista = [...(planos.dados ?? [])].sort((a, b) => a.preco - b.preco)
  const maisCaro = lista.length > 1 ? lista[lista.length - 1].id : null
  const assin = (assinaturas.dados ?? []).filter((a) => !filtroStatus || a.status === filtroStatus)

  const mudar = <K extends keyof PlanoForm>(campo: K, valor: PlanoForm[K]) =>
    setForm((f) => (f ? { ...f, dados: { ...f.dados, [campo]: valor } } : f))
  const numeroOuNulo = (v: string) => (v === '' ? null : Number(v))

  async function salvar() {
    if (!form) return
    setSalvando(true)
    setErroModal(null)
    try {
      if (form.id == null) await adminApi.criarPlano(form.dados)
      else await adminApi.atualizarPlano(form.id, form.dados)
      setForm(null)
      await planos.recarregar()
    } catch (e) {
      setErroModal(mensagemErro(e, 'Falha ao salvar o plano.'))
    } finally {
      setSalvando(false)
    }
  }

  async function acao(a: Assinatura, tipo: 'cancelar' | 'renovar') {
    setErroAcao(null)
    try {
      await (tipo === 'cancelar' ? adminApi.cancelarAssinatura(a.id) : adminApi.renovarAssinatura(a.id))
      await assinaturas.recarregar()
    } catch (e) {
      setErroAcao(mensagemErro(e, 'Falha ao atualizar a assinatura.'))
    }
  }

  return (
    <>
      <PageHeader titulo="Planos & Assinaturas">
        <button type="button" className="btn primary with-icon" onClick={() => { setErroModal(null); setForm({ id: null, dados: vazio }) }}>
          <Plus /> Novo Plano
        </button>
      </PageHeader>
      {(planos.erro || assinaturas.erro) && <p className="error" role="alert">{planos.erro ?? assinaturas.erro}</p>}

      <div className="plan-grid">
        {lista.map((p) => (
          <section key={p.id} className={`card plan ${p.id === maisCaro ? 'dark' : ''}`}>
            <h2>{p.nome} {!p.ativo && <span className="badge off">Inativo</span>}</h2>
            <div className="plan-price">
              {p.preco === 0 ? 'R$ 0' : formatarMoeda(p.preco)}<span>/mês</span>
            </div>
            <ul>
              {destaques(p).map((d) => <li key={d}><CircleCheck />{d}</li>)}
            </ul>
            <button type="button" className="btn plan-btn" onClick={() => { setErroModal(null); setForm({ id: p.id, dados: { ...p } }) }}>
              Editar Plano
            </button>
          </section>
        ))}
        {!planos.carregando && lista.length === 0 && <p className="muted">Nenhum plano cadastrado.</p>}
      </div>

      <div className="section-head">
        <h2>Assinaturas</h2>
        <select className="select" value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value)} aria-label="Filtrar por status">
          <option value="">Status: Todos</option>
          <option value="ATIVA">Ativa</option>
          <option value="INADIMPLENTE">Inadimplente</option>
          <option value="CANCELADA">Cancelada</option>
          <option value="EXPIRADA">Expirada</option>
        </select>
      </div>
      {erroAcao && <p className="error" role="alert">{erroAcao}</p>}
      <div className="card table-card">
        <table className="table">
          <thead>
            <tr><th>Usuário</th><th>E-mail</th><th>Plano</th><th>Status</th><th>Início</th><th>Fim</th><th className="center">Ações</th></tr>
          </thead>
          <tbody>
            {assinaturas.carregando && <tr><td colSpan={7} className="muted">Carregando…</td></tr>}
            {!assinaturas.carregando && assin.length === 0 && <tr><td colSpan={7} className="muted">Nenhuma assinatura encontrada.</td></tr>}
            {assin.map((a) => (
              <tr key={a.id}>
                <td className="strong">{a.usuarioNome}</td>
                <td>{a.usuarioEmail}</td>
                <td>{a.planoNome}</td>
                <td><Badge valor={a.status} /></td>
                <td>{formatarData(a.dataInicio)}</td>
                <td>{formatarData(a.dataFim)}</td>
                <td className="center">
                  <button type="button" className="link-btn" onClick={() => acao(a, 'renovar')}>Renovar</button>
                  {a.status !== 'CANCELADA' && (
                    <button type="button" className="link-btn danger" onClick={() => acao(a, 'cancelar')}>Cancelar</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {form && (
        <Modal titulo={form.id == null ? 'Novo plano' : `Editar plano ${form.dados.nome}`} onClose={() => setForm(null)}>
          <label className="field">Nome
            <input className="input" value={form.dados.nome} onChange={(e) => mudar('nome', e.target.value)} />
          </label>
          <label className="field">Preço (R$/mês)
            <input className="input" type="number" min="0" step="0.01" value={form.dados.preco}
              onChange={(e) => mudar('preco', Number(e.target.value))} />
          </label>
          <div className="two-cols">
            <label className="field">Coletas / mês
              <input className="input" type="number" min="0" placeholder="Ilimitado" value={form.dados.limiteColetasMes ?? ''}
                onChange={(e) => mudar('limiteColetasMes', numeroOuNulo(e.target.value))} />
            </label>
            <label className="field">Coletas / semana
              <input className="input" type="number" min="0" placeholder="Sem limite" value={form.dados.limiteColetasSemana ?? ''}
                onChange={(e) => mudar('limiteColetasSemana', numeroOuNulo(e.target.value))} />
            </label>
          </div>
          <label className="field">Benefícios (um por linha)
            <textarea className="input" rows={4} value={form.dados.beneficios} onChange={(e) => mudar('beneficios', e.target.value)} />
          </label>
          <label className="check">
            <input type="checkbox" checked={form.dados.ativo} onChange={(e) => mudar('ativo', e.target.checked)} />
            Plano ativo
          </label>
          {erroModal && <p className="error" role="alert">{erroModal}</p>}
          <div className="actions end">
            <button type="button" className="btn" onClick={() => setForm(null)}>Cancelar</button>
            <button type="button" className="btn primary" onClick={salvar} disabled={salvando}>
              {salvando ? 'Salvando…' : 'Salvar'}
            </button>
          </div>
        </Modal>
      )}
    </>
  )
}
