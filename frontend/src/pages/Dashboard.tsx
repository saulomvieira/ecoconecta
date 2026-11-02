import { CreditCard, ShieldCheck, Trash2, Users } from 'lucide-react'
import { adminApi, tempoRelativo } from '../api/admin'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useCarregar } from '../components/useCarregar'

const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']
const rotuloMes = (ym: string) => MESES[Number(ym.slice(5, 7)) - 1] ?? ym

export function DashboardPage() {
  const stats = useCarregar(adminApi.dashboard)
  const mensais = useCarregar(adminApi.residuosMensais)
  const atividades = useCarregar(adminApi.atividades)

  const ultimos = (mensais.dados ?? []).slice(-6)
  const maximo = Math.max(1, ...ultimos.map((m) => m.total))
  const erro = stats.erro ?? mensais.erro ?? atividades.erro

  return (
    <>
      <PageHeader titulo="Dashboard" />
      {erro && <p className="error" role="alert">{erro}</p>}

      <div className="stat-grid cols-4">
        <StatCard icon={Users} valor={stats.dados?.totalUsuarios ?? '—'} rotulo="Total de Usuários" />
        <StatCard icon={Trash2} valor={stats.dados?.totalResiduos ?? '—'} rotulo="Resíduos Publicados" />
        <StatCard icon={ShieldCheck} valor={stats.dados?.reservasAtivas ?? '—'} rotulo="Reservas Ativas" />
        <StatCard icon={CreditCard} valor={stats.dados?.assinaturasAtivas ?? '—'} rotulo="Assinaturas Ativas" />
      </div>

      <div className="dash-grid">
        <section className="card panel">
          <h2>Resíduos por Mês (Últimos 6 meses)</h2>
          {mensais.carregando ? (
            <p className="muted">Carregando…</p>
          ) : ultimos.length === 0 ? (
            <p className="muted">Sem dados no período.</p>
          ) : (
            <div className="bars">
              {ultimos.map((m, i) => (
                <div key={m.mes} className="bar-col">
                  <span className="bar-value">{m.total}</span>
                  <div
                    className="bar"
                    style={{ height: `${Math.max(4, (m.total / maximo) * 220)}px`, opacity: 0.45 + (0.55 * (i + 1)) / ultimos.length }}
                  />
                  <span className="bar-label">{rotuloMes(m.mes)}</span>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="card panel">
          <h2>Atividade Recente</h2>
          {atividades.carregando ? (
            <p className="muted">Carregando…</p>
          ) : (
            <ul className="activity">
              {(atividades.dados ?? []).slice(0, 6).map((a, i) => (
                <li key={i}>
                  <span>{a.descricao}</span>
                  <span className="muted small">{tempoRelativo(a.data)}</span>
                </li>
              ))}
              {atividades.dados?.length === 0 && <li className="muted">Nenhuma atividade ainda.</li>}
            </ul>
          )}
        </section>
      </div>
    </>
  )
}
