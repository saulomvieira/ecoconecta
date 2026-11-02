export function PageHeader({ titulo, children }: { titulo: string; children?: React.ReactNode }) {
  return (
    <header className="page-header">
      <h1>{titulo}</h1>
      {children && <div className="page-header-actions">{children}</div>}
    </header>
  )
}
