import { Leaf } from 'lucide-react'
import type { LucideProps } from 'lucide-react'

// Ícone oficial da marca EcoConecta. Use este componente em vez de importar um ícone do lucide
// direto, para a marca ficar igual em todas as telas (o favicon usa o mesmo desenho).
export function LogoIcone({ strokeWidth = 1.75, ...props }: LucideProps) {
  return <Leaf strokeWidth={strokeWidth} aria-hidden="true" {...props} />
}
