
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  listarFeed,
  type ResiduoDisponibilidade,
} from '../api/residuos'
import './DetalhesResiduo.css'

export function DetalhesResiduo() {
  const { id } = useParams<{ id: string }>()

  const [item, setItem] = useState<ResiduoDisponibilidade | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true

    async function carregarDetalhes() {
      try {
        const dados = await listarFeed()
        const encontrado = dados.find(
          ({ residuo }) => String(residuo.id) === id
        )

        if (ativo) {
          setItem(encontrado ?? null)
        }
      } catch {
        if (ativo) {
          setErro('Não foi possível carregar os detalhes do resíduo.')
        }
      } finally {
        if (ativo) setCarregando(false)
      }
    }

    carregarDetalhes()

    return () => {
      ativo = false
    }
  }, [id])

  if (carregando) {
    return (
      <main className="detalhes-container">
        <p>Carregando detalhes...</p>
      </main>
    )
  }

  if (erro) {
    return (
      <main className="detalhes-container">
        <p role="alert">{erro}</p>
        <Link to="/feed">Voltar ao feed</Link>
      </main>
    )
  }

  if (!item) {
    return (
      <main className="detalhes-container">
        <h1>Resíduo não encontrado</h1>
        <p>O resíduo solicitado não está disponível no feed.</p>
        <Link to="/feed">Voltar ao feed</Link>
      </main>
    )
  }

  const { residuo, status } = item

  const imagemUrl = residuo.imagemUrl
    ? residuo.imagemUrl.startsWith('/residuos/')
      ? `/api${residuo.imagemUrl}`
      : residuo.imagemUrl
    : null

  return (
    <main className="detalhes-container">
      <Link to="/feed" className="detalhes-voltar">
        ← Voltar ao feed
      </Link>

      <article className="detalhes-card">
        {imagemUrl && (
          <img
            src={imagemUrl}
            alt={residuo.nome}
            className="detalhes-imagem"
          />
        )}

        <div className="detalhes-conteudo">
          <span className="residuo-categoria">
            {residuo.categoria}
          </span>

          <h1>{residuo.nome}</h1>

          <span className="residuo-status">
            {status}
          </span>

          <h2>Descrição</h2>
          <p>{residuo.descricao}</p>

          <h2>Informações do resíduo</h2>

          <dl className="detalhes-informacoes">
            <div>
              <dt>Quantidade</dt>
              <dd>
                {residuo.quantidade} {residuo.unidadeMedida}
              </dd>
            </div>

            <div>
              <dt>Categoria</dt>
              <dd>{residuo.categoria}</dd>
            </div>

            <div>
              <dt>Região</dt>
              <dd>{residuo.regiao}</dd>
            </div>

            <div>
              <dt>Data de cadastro</dt>
              <dd>
                {residuo.dataCadastro
                  ? new Date(residuo.dataCadastro).toLocaleDateString('pt-BR')
                  : 'Não informada'}
              </dd>
            </div>
          </dl>

          <p className="detalhes-aviso">
            A solicitação de reserva estará disponível em uma
            próxima etapa.
          </p>
        </div>
      </article>
    </main>
  )
}