import { useCallback, useEffect, useState } from 'react'
import { mensagemErro } from '../api/admin'

// Carrega dados ao montar e expõe `recarregar` para depois de mutações.
export function useCarregar<T>(buscar: () => Promise<T>) {
  const [dados, setDados] = useState<T | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [carregando, setCarregando] = useState(true)

  const recarregar = useCallback(async () => {
    try {
      setDados(await buscar())
      setErro(null)
    } catch (e) {
      setErro(mensagemErro(e, 'Falha ao carregar os dados.'))
    } finally {
      setCarregando(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    recarregar()
  }, [recarregar])

  return { dados, erro, carregando, recarregar }
}
