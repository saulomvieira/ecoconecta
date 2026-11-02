import { useState } from 'react'
import { Pencil, Search, ShieldCheck, Trash2, Users } from 'lucide-react'
import { adminApi, mensagemErro, type Perfil, type Usuario } from '../api/admin'
import { Badge } from '../components/Badge'
import { Modal } from '../components/Modal'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useCarregar } from '../components/useCarregar'

const semAcento = (s: string) => s.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()

export function UsuariosPage() {
  const { dados, erro, carregando, recarregar } = useCarregar(adminApi.usuarios)
  const [busca, setBusca] = useState('')
  const [filtroPerfil, setFiltroPerfil] = useState('')
  const [filtroStatus, setFiltroStatus] = useState('')
  const [editando, setEditando] = useState<Usuario | null>(null)
  const [perfil, setPerfil] = useState<Perfil>('USUARIO')
  const [ativo, setAtivo] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erroModal, setErroModal] = useState<string | null>(null)

  const usuarios = dados ?? []
  const termo = semAcento(busca.trim())
  const filtrados = usuarios.filter(
      (u) =>
        (!termo || semAcento(u.nome).includes(termo) || semAcento(u.email).includes(termo)) &&
        (!filtroPerfil || u.perfil === filtroPerfil) &&
        (!filtroStatus || String(u.ativo) === filtroStatus),
  )

  function abrir(u: Usuario) {
    setEditando(u)
    setPerfil(u.perfil)
    setAtivo(u.ativo)
    setErroModal(null)
  }

  async function salvar() {
    if (!editando) return
    setSalvando(true)
    setErroModal(null)
    try {
      if (perfil !== editando.perfil) await adminApi.alterarPerfil(editando.id, perfil)
      if (ativo !== editando.ativo) await adminApi.alterarAtivo(editando.id, ativo)
      setEditando(null)
      await recarregar()
    } catch (e) {
      setErroModal(mensagemErro(e, 'Falha ao salvar.'))
      await recarregar()
    } finally {
      setSalvando(false)
    }
  }

  return (
    <>
      <PageHeader titulo="Usuários">
        <label className="search">
          <Search />
          <input value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar por nome ou e-mail" />
        </label>
        <select className="select" value={filtroPerfil} onChange={(e) => setFiltroPerfil(e.target.value)} aria-label="Filtrar por perfil">
          <option value="">Perfil: Todos</option>
          <option value="USUARIO">Usuário</option>
          <option value="ADMIN">Administrador</option>
        </select>
        <select className="select" value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value)} aria-label="Filtrar por status">
          <option value="">Status: Todos</option>
          <option value="true">Ativo</option>
          <option value="false">Inativo</option>
        </select>
      </PageHeader>

      <div className="stat-grid cols-3">
        <StatCard icon={Users} valor={usuarios.length} rotulo="Usuários cadastrados" />
        <StatCard icon={ShieldCheck} valor={usuarios.filter((u) => u.ativo).length} rotulo="Usuários ativos" />
        <StatCard icon={Trash2} valor={usuarios.filter((u) => u.perfil === 'ADMIN').length} rotulo="Administradores" />
      </div>

      {erro && <p className="error" role="alert">{erro}</p>}

      <div className="card table-card">
        <table className="table">
          <thead>
            <tr><th>Nome</th><th>E-mail</th><th>Região</th><th>Perfil</th><th>Status</th><th className="center">Ações</th></tr>
          </thead>
          <tbody>
            {carregando && <tr><td colSpan={6} className="muted">Carregando…</td></tr>}
            {!carregando && filtrados.length === 0 && <tr><td colSpan={6} className="muted">Nenhum usuário encontrado.</td></tr>}
            {filtrados.map((u) => (
              <tr key={u.id}>
                <td className="strong">{u.nome}</td>
                <td>{u.email}</td>
                <td>{u.regiao ?? '—'}</td>
                <td>{u.perfil === 'ADMIN' ? 'Administrador' : 'Usuário'}</td>
                <td><Badge valor={u.ativo ? 'ATIVO' : 'INATIVO'} /></td>
                <td className="center">
                  <button type="button" className="icon-btn" onClick={() => abrir(u)} aria-label={`Editar ${u.nome}`} title="Editar">
                    <Pencil />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="table-count muted">
        Exibindo <strong>{filtrados.length}</strong> de <strong>{usuarios.length}</strong> usuários
      </p>

      {editando && (
        <Modal titulo={`Editar ${editando.nome}`} onClose={() => setEditando(null)}>
          <label className="field">
            Perfil
            <select className="select" value={perfil} onChange={(e) => setPerfil(e.target.value as Perfil)}>
              <option value="USUARIO">Usuário</option>
              <option value="ADMIN">Administrador</option>
            </select>
          </label>
          <label className="check">
            <input type="checkbox" checked={ativo} onChange={(e) => setAtivo(e.target.checked)} />
            Conta ativa
          </label>
          {erroModal && <p className="error" role="alert">{erroModal}</p>}
          <div className="actions end">
            <button type="button" className="btn" onClick={() => setEditando(null)}>Cancelar</button>
            <button type="button" className="btn primary" onClick={salvar} disabled={salvando}>
              {salvando ? 'Salvando…' : 'Salvar'}
            </button>
          </div>
        </Modal>
      )}
    </>
  )
}
