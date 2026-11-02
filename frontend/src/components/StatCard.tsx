import type { LucideIcon } from 'lucide-react'

export function StatCard({ icon: Icon, valor, rotulo }: { icon: LucideIcon; valor: number | string; rotulo: string }) {
  return (
    <div className="card stat-card">
      <span className="stat-icon"><Icon /></span>
      <div>
        <strong>{typeof valor === 'number' ? valor.toLocaleString('pt-BR') : valor}</strong>
        <span>{rotulo}</span>
      </div>
    </div>
  )
}
