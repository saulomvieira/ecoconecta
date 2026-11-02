import { Leaf } from 'lucide-react'
import type { LucideProps } from 'lucide-react'

export function LogoIcone({ strokeWidth = 1.75, ...props }: LucideProps) {
  return <Leaf strokeWidth={strokeWidth} aria-hidden="true" {...props} />
}
