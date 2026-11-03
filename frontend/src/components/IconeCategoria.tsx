import { Apple, Package, Recycle } from 'lucide-react'
import { semAcento } from '../utils/tempo'

export function IconeCategoria({ categoria, className }: { categoria: string; className?: string }) {
  const nome = semAcento(categoria)
  const props = { className, strokeWidth: 1.75, 'aria-hidden': true } as const
  if (/organic|compost|alimento|fruta|verdura/.test(nome)) return <Apple {...props} />
  if (/recicl|papel|papelao|metal|plastico|vidro|pet|aluminio|ferro/.test(nome)) return <Recycle {...props} />
  return <Package {...props} />
}
