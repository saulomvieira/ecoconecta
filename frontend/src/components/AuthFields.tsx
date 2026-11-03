import { useState, type InputHTMLAttributes } from 'react'
import { Eye, EyeOff, Lock, Mail, type LucideIcon } from 'lucide-react'

type Props = InputHTMLAttributes<HTMLInputElement> & { rotulo: string; icone: LucideIcon; obrigatorio?: boolean }

export function CampoTexto({ rotulo, icone: Icone, obrigatorio, children, ...input }: Props & { children?: React.ReactNode }) {
  return (
    <label className="auth-field">
      <span className="auth-label">
        {rotulo}
        {obrigatorio && <b aria-hidden="true"> *</b>}
      </span>
      <span className="auth-input">
        <Icone />
        <input required={obrigatorio} {...input} />
        {children}
      </span>
    </label>
  )
}

export function CampoEmail(props: Omit<Props, 'icone' | 'type'>) {
  return <CampoTexto icone={Mail} type="email" autoComplete="email" placeholder="seu@email.com" {...props} />
}

export function CampoSenha({ autoComplete = 'current-password', ...props }: Omit<Props, 'icone' | 'type'>) {
  const [visivel, setVisivel] = useState(false)
  return (
    <CampoTexto icone={Lock} type={visivel ? 'text' : 'password'} autoComplete={autoComplete} {...props}>
      <button
        type="button"
        className="eye"
        onClick={() => setVisivel((v) => !v)}
        aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
      >
        {visivel ? <EyeOff /> : <Eye />}
      </button>
    </CampoTexto>
  )
}
