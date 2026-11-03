import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { listarFeed, type ResiduoDisponibilidade } from '../api/residuos'
import { DetalhesResiduo } from './DetalhesResiduo'
import { FeedResiduos } from './FeedResiduos'

export function FeedComDetalhes() {
  const { id } = useParams<{ id: string }>()
  const [residuos, setResiduos] = useState<ResiduoDisponibilidade[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true
    listarFeed()
      .then((dados) => ativo && setResiduos(dados))
      .catch(() => ativo && setErro('Não foi possível carregar os resíduos.'))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [])

  function marcarComoReservado(residuoId: number) {
    setResiduos((atuais) =>
      atuais.map((item) => (item.residuo.id === residuoId ? { ...item, status: 'RESERVADO' } : item)),
    )
  }

  return (
    <>
      <FeedResiduos residuos={residuos} carregando={carregando} erro={erro} />
      {id && (
        <DetalhesResiduo
          key={id}
          residuos={residuos}
          carregando={carregando}
          erro={erro}
          onReservado={marcarComoReservado}
        />
      )}
    </>
  )
}
