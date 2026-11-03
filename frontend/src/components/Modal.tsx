export function Modal({ titulo, onClose, largo, children }: { titulo: string; onClose: () => void; largo?: boolean; children: React.ReactNode }) {
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className={`card modal${largo ? ' largo' : ''}`} role="dialog" aria-modal="true" aria-label={titulo} onClick={(e) => e.stopPropagation()}>
        <h2>{titulo}</h2>
        {children}
      </div>
    </div>
  )
}
