import { PageHeader } from '../components/PageHeader'

export function EmBreve({ titulo }: { titulo: string }) {
  return (
    <>
      <PageHeader titulo={titulo} />
      <p className="muted">Esta tela ainda não foi implementada.</p>
    </>
  )
}
