import { LogoIcone } from './LogoIcone'

// Cartão central usado por cadastro e recuperação de senha.
export function AuthCard({ titulo, subtitulo, children }: { titulo: string; subtitulo?: string; children: React.ReactNode }) {
  return (
    <div className="auth-page">
      <div className="card auth-card">
        <h1><LogoIcone /> {titulo}</h1>
        {subtitulo && <p className="auth-sub">{subtitulo}</p>}
        {children}
      </div>
    </div>
  )
}
