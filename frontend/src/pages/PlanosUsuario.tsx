import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { Check, Crown, Leaf, RefreshCw } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import {
  assinarPlano,
  cancelarMinhaAssinatura,
  consultarMinhasAssinaturas,
  consultarPlanos,
  type MinhaAssinatura,
  type PlanoUsuario,
} from '../api/planosUsuario'
import './PlanosUsuario.css'

const reais = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

function formatarPreco(valor: number): string {
  return valor === 0 ? 'R$ 0' : reais.format(valor)
}

function categoriaPlano(plano: PlanoUsuario): 'gratis' | 'basico' | 'plus' {
  if (plano.preco === 0) return 'gratis'
  return plano.nome.toLocaleLowerCase('pt-BR').includes('plus') ? 'plus' : 'basico'
}

function vantagens(plano: PlanoUsuario): string[] {
  const itens: string[] = []

  itens.push(
    plano.limiteColetasMes === null
      ? 'Coletas ilimitadas'
      : `${plano.limiteColetasMes} coletas por mês`,
  )

  if (plano.limiteColetasSemana !== null) {
    itens.push(
      `Máx. ${plano.limiteColetasSemana} ${plano.limiteColetasSemana === 1 ? 'coleta' : 'coletas'} por semana`,
    )
  } else if (plano.limiteColetasMes === null && categoriaPlano(plano) === 'basico') {
    itens.push('Sem limite semanal')
  }

  if (plano.beneficios) {
    itens.push(...plano.beneficios.split(/\r?\n|;/).map((texto) => texto.trim()).filter(Boolean))
  }

  return [...new Set(itens)]
}

function formatarDataISO(data: string | null): string | null {
  if (!data) return null
  const partes = data.split('-')
  return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : data
}

export function PlanosUsuario() {
  const { sessao } = useAuth()
  const usuarioId = sessao?.id

  const [planos, setPlanos] = useState<PlanoUsuario[]>([])
  const [assinaturas, setAssinaturas] = useState<MinhaAssinatura[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [processando, setProcessando] = useState(false)

  const carregar = useCallback(async () => {
    if (!usuarioId) return
    setCarregando(true)
    setErro('')

    try {
      const [planosRecebidos, assinaturasRecebidas] = await Promise.all([
        consultarPlanos(),
        consultarMinhasAssinaturas(),
      ])
      setPlanos(planosRecebidos)
      setAssinaturas(assinaturasRecebidas)
    } catch (error) {
      setErro(error instanceof Error ? error.message : 'Não foi possível consultar seus planos.')
    } finally {
      setCarregando(false)
    }
  }, [usuarioId])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const assinaturaAtiva = useMemo(
    () => assinaturas.find((item) => item.status === 'ATIVA') ?? null,
    [assinaturas],
  )

  const planosVisiveis = useMemo(() => {
    const planosAtivos = planos.filter((plano) => plano.ativo)
    // Mesmo se for desativado no painel admin, o plano do usuário continua visível.
    if (assinaturaAtiva && !planosAtivos.some((plano) => plano.id === assinaturaAtiva.planoId)) {
      const anterior = planos.find((plano) => plano.id === assinaturaAtiva.planoId)
      if (anterior) planosAtivos.push(anterior)
    }
    return planosAtivos.sort((a, b) => a.preco - b.preco)
  }, [planos, assinaturaAtiva])

  const planoGratuito = planosVisiveis.find((plano) => plano.preco === 0) ?? null
  const planoAtualId = assinaturaAtiva?.planoId ?? planoGratuito?.id ?? null
  const assinaturaPago = !!(assinaturaAtiva && planos.find((plano) => plano.id === assinaturaAtiva.planoId)?.preco !== 0)

  async function contratar(plano: PlanoUsuario) {
    if (!usuarioId || processando || carregando || erro) return
    if (plano.preco === 0) return

    // O backend só permite uma assinatura ativa por usuário.
    if (assinaturaAtiva) {
      setErro('Para trocar de plano, cancele primeiro a assinatura atual.')
      return
    }

    if (!window.confirm(
      `Confirmar assinatura do plano ${plano.nome} por ${formatarPreco(plano.preco)}/mês?\n\n` +
      'Esta é uma simulação acadêmica. Não haverá cobrança real.',
    )) return

    setProcessando(true)
    setErro('')
    setAviso('')

    try {
      await assinarPlano(usuarioId, plano.id)
      await carregar()
      setAviso(`Plano ${plano.nome} ativado no ambiente de demonstração.`)
    } catch (error) {
      setErro(error instanceof Error ? error.message : 'Não foi possível ativar a assinatura.')
    } finally {
      setProcessando(false)
    }
  }

  async function cancelar() {
    if (!assinaturaAtiva || processando || carregando || erro) return

    if (!window.confirm(
      'Cancelar sua assinatura atual? O cancelamento é imediato no ambiente de demonstração e você voltará ao plano Gratuito.',
    )) return

    setProcessando(true)
    setErro('')
    setAviso('')

    try {
      await cancelarMinhaAssinatura(assinaturaAtiva.id)
      await carregar()
      setAviso('Assinatura cancelada. Seu plano agora é o Gratuito.')
    } catch (error) {
      setErro(error instanceof Error ? error.message : 'Não foi possível cancelar sua assinatura.')
    } finally {
      setProcessando(false)
    }
  }

  if (!sessao) {
    return <Navigate to="/login" replace state={{ from: '/planos' }} />
  }

  return (
    <main className="planos-usuario">
      <header className="planos-usuario-titulo">
        <h1>Escolha o plano ideal para coletar sem limites</h1>
        <p>Cobrança recorrente via gateway de pagamento – simulada no ambiente acadêmico (RF17)</p>
      </header>

      {carregando && <p className="planos-usuario-mensagem" role="status">Carregando planos e assinatura...</p>}
      {erro && (
        <div className="planos-usuario-aviso erro" role="alert">
          <p>{erro}</p>
          <button type="button" onClick={() => { setErro(''); void carregar() }}>Tentar novamente</button>
        </div>
      )}
      {aviso && !erro && <p className="planos-usuario-aviso sucesso" role="status">{aviso}</p>}

      {!carregando && !erro && planosVisiveis.length === 0 && (
        <div className="planos-usuario-aviso">Nenhum plano ativo foi encontrado. Consulte a administração.</div>
      )}

      {!carregando && !erro && planosVisiveis.length > 0 && (
        <>
          <section className="planos-usuario-grade" aria-label="Planos disponíveis">
            {planosVisiveis.map((plano) => {
              const categoria = categoriaPlano(plano)
              const atual = plano.id === planoAtualId
              const gratuito = plano.preco === 0
              const indisponivel = !!assinaturaAtiva && !atual

              return (
                <article
                  key={plano.id}
                  className={`planos-usuario-card planos-usuario-${categoria}${atual ? ' plano-atual' : ''}`}
                >
                  <div className="planos-usuario-card-topo">
                    <div className="planos-usuario-icone" aria-hidden="true">
                      {categoria === 'gratis' ? <Leaf /> : categoria === 'plus' ? <Crown /> : <RefreshCw />}
                    </div>
                    <h2>{plano.nome}</h2>
                    <div className="planos-usuario-preco">
                      <strong>{formatarPreco(plano.preco)}</strong><span>/mês</span>
                    </div>
                  </div>

                  <ul className="planos-usuario-vantagens">
                    {vantagens(plano).map((vantagem, indice) => (
                      <li key={`${indice}-${vantagem}`}><Check aria-hidden="true" /><span>{vantagem}</span></li>
                    ))}
                  </ul>

                  <div className="planos-usuario-rodape">
                    {atual ? (
                      <button className="planos-usuario-botao atual" type="button" disabled>Plano atual</button>
                    ) : gratuito ? (
                      <button
                        className="planos-usuario-botao secundario"
                        type="button"
                        disabled={!assinaturaPago || processando}
                        onClick={() => void cancelar()}
                      >
                        Voltar ao Gratuito
                      </button>
                    ) : (
                      <button
                        className={`planos-usuario-botao ${categoria === 'plus' ? 'destaque' : 'secundario'}`}
                        type="button"
                        disabled={processando || indisponivel || !plano.ativo}
                        onClick={() => void contratar(plano)}
                      >
                        {processando ? 'Processando...' : indisponivel ? 'Cancele o plano atual' : `Assinar ${plano.nome}`}
                      </button>
                    )}
                  </div>
                </article>
              )
            })}
          </section>

          {assinaturaAtiva && (
            <section className="planos-usuario-gerenciar" aria-label="Gerenciar minha assinatura">
              <div>
                <strong>Minha assinatura: {assinaturaAtiva.planoNome}</strong>
                <p>
                  Status: ativa
                  {assinaturaAtiva.dataFim ? ` · Vigência até ${formatarDataISO(assinaturaAtiva.dataFim)}` : ''}
                </p>
                <small>Para mudar de plano, cancele a assinatura atual antes de contratar outra. O cancelamento é imediato.</small>
              </div>
              <button type="button" disabled={processando} onClick={() => void cancelar()}>
                {processando ? 'Processando...' : 'Cancelar assinatura'}
              </button>
            </section>
          )}
        </>
      )}

      <footer className="planos-usuario-nota">
        <p>Assinatura inadimplente retorna automaticamente ao plano Gratuito (RGN11).</p>
        <Link to="/feed">Voltar ao feed</Link>
      </footer>
    </main>
  )
}
