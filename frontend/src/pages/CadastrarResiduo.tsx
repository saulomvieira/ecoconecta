import { useEffect, useRef, useState, type ChangeEvent, type FormEvent } from 'react'
import { UploadCloud, X } from 'lucide-react'
import { mensagemErro } from '../api/admin'
import { REGIOES, rotuloRegiao } from '../api/regioes'
import {
  atualizarResiduo,
  cadastrarResiduo,
  enviarImagemResiduo,
  type Residuo,
} from '../api/residuos'
import { urlImagem } from '../utils/residuo'
import './CadastrarResiduo.css'

const CATEGORIAS = ['Orgânico', 'Reciclável', 'Outro']
const UNIDADES = ['kg', 'g', 'L', 'un']
const TIPOS_IMAGEM = ['image/jpeg', 'image/png', 'image/webp']
const TAMANHO_MAX = 5 * 1024 * 1024

interface Props {
  residuo?: Residuo
  onClose: () => void
  onSalvo: () => void
}

export function CadastrarResiduo({ residuo, onClose, onSalvo }: Props) {
  const inputArquivo = useRef<HTMLInputElement>(null)
  const [nome, setNome] = useState(residuo?.nome ?? '')
  const [descricao, setDescricao] = useState(residuo?.descricao ?? '')
  const [categoria, setCategoria] = useState(residuo?.categoria ?? '')
  const [quantidade, setQuantidade] = useState(residuo ? String(residuo.quantidade) : '')
  const [unidade, setUnidade] = useState(residuo?.unidadeMedida ?? 'kg')
  const [regiao, setRegiao] = useState(residuo?.regiao ?? '')
  const [endereco, setEndereco] = useState(residuo?.enderecoColeta ?? '')
  const [imagem, setImagem] = useState<File | null>(null)
  const [previa, setPrevia] = useState<string | null>(null)
  const [arrastando, setArrastando] = useState(false)
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  const editando = residuo != null
  const imagemAtual = residuo ? urlImagem(residuo) : null

  useEffect(() => () => { if (previa) URL.revokeObjectURL(previa) }, [previa])

  useEffect(() => {
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && !enviando && onClose()
    document.addEventListener('keydown', esc)
    return () => document.removeEventListener('keydown', esc)
  }, [onClose, enviando])

  function aceitarArquivo(arquivo?: File) {
    if (!arquivo) return
    if (!TIPOS_IMAGEM.includes(arquivo.type)) {
      setErro('Use uma imagem JPG, PNG ou WebP.')
    } else if (arquivo.size > TAMANHO_MAX) {
      setErro('A imagem deve ter no máximo 5 MB.')
    } else {
      setErro('')
      setImagem(arquivo)
      setPrevia(URL.createObjectURL(arquivo))
    }
  }

  function escolherImagem(e: ChangeEvent<HTMLInputElement>) {
    aceitarArquivo(e.target.files?.[0])
    e.target.value = ''
  }

  function removerImagem() {
    setImagem(null)
    setPrevia(null)
  }

  async function enviar(e: FormEvent) {
    e.preventDefault()
    const qtd = Number(quantidade.replace(',', '.'))
    if (!categoria) {
      setErro('Escolha a classificação do resíduo.')
      return
    }
    if (!(qtd > 0)) {
      setErro('Informe uma quantidade maior que zero.')
      return
    }
    setErro('')
    setEnviando(true)
    const dados = {
      nome: nome.trim(),
      descricao: descricao.trim(),
      categoria,
      quantidade: qtd,
      unidadeMedida: unidade,
      regiao,
      enderecoColeta: endereco.trim(),
    }
    try {
      const salvo = editando
        ? await atualizarResiduo(residuo.id, { ...dados, imagemUrl: residuo.imagemUrl })
        : await cadastrarResiduo(dados)
      if (imagem) {
        try {
          await enviarImagemResiduo(salvo.id, imagem)
        } catch (err) {
          setErro(`Resíduo salvo, mas a imagem não foi enviada: ${mensagemErro(err, 'tente novamente')}`)
          setEnviando(false)
          return
        }
      }
      onSalvo()
    } catch (err) {
      setErro(mensagemErro(err, 'Não foi possível salvar o resíduo.'))
      setEnviando(false)
    }
  }

  const miniatura = previa ?? imagemAtual

  return (
    <div className="modal-backdrop cad-backdrop" onClick={() => !enviando && onClose()}>
      <div className="cad-modal" role="dialog" aria-modal="true" aria-label={editando ? 'Editar resíduo' : 'Cadastrar resíduo'} onClick={(e) => e.stopPropagation()}>
        <header className="cad-cabecalho">
          <h2>{editando ? 'Editar resíduo' : 'Cadastrar resíduo'}</h2>
          <button type="button" className="cad-fechar" onClick={onClose} disabled={enviando} aria-label="Fechar"><X /></button>
        </header>

        <form className="cad-form" onSubmit={enviar}>
          <label className="cad-campo">
            <span className="cad-rotulo">Nome <i>*</i></span>
            <input className="cad-input" value={nome} onChange={(e) => setNome(e.target.value)} maxLength={120} required placeholder="Ex.: Restos de frutas e verduras" />
          </label>

          <label className="cad-campo">
            <span className="cad-rotulo">Descrição <i>*</i></span>
            <textarea className="cad-input cad-textarea" value={descricao} onChange={(e) => setDescricao(e.target.value)} maxLength={1000} required placeholder="Ex.: restos de frutas e verduras para compostagem..." />
          </label>

          <div className="cad-campo">
            <span className="cad-rotulo">Classificação <i>*</i></span>
            <div className="cad-pilulas" role="radiogroup" aria-label="Classificação">
              {CATEGORIAS.map((c) => (
                <button key={c} type="button" role="radio" aria-checked={categoria === c} className={`cad-pilula${categoria === c ? ' ativa' : ''}`} onClick={() => setCategoria(c)}>{c}</button>
              ))}
            </div>
          </div>

          <div className="cad-grade">
            <label className="cad-campo">
              <span className="cad-rotulo">Quantidade <i>*</i></span>
              <input className="cad-input" inputMode="decimal" value={quantidade} onChange={(e) => setQuantidade(e.target.value)} required placeholder="0" />
            </label>
            <label className="cad-campo">
              <span className="cad-rotulo">Unidade <i>*</i></span>
              <select className="cad-input" value={unidade} onChange={(e) => setUnidade(e.target.value)}>
                {UNIDADES.map((u) => <option key={u} value={u}>{u}</option>)}
              </select>
            </label>
          </div>

          <div className="cad-grade">
            <label className="cad-campo">
              <span className="cad-rotulo">Região <i>*</i></span>
              <select className="cad-input" value={regiao} onChange={(e) => setRegiao(e.target.value)} required>
                <option value="" disabled>Selecione</option>
                {regiao && !REGIOES.includes(regiao) && <option value={regiao}>{regiao}</option>}
                {REGIOES.map((r) => <option key={r} value={r}>{rotuloRegiao(r).replace(', CE', '')}</option>)}
              </select>
            </label>
            <label className="cad-campo">
              <span className="cad-rotulo">Endereço de coleta <i>*</i></span>
              <input className="cad-input" value={endereco} onChange={(e) => setEndereco(e.target.value)} required placeholder="Rua, número..." />
            </label>
          </div>

          <div className="cad-campo">
            <span className="cad-rotulo">Imagem (opcional)</span>
            {miniatura ? (
              <div className="cad-previa">
                <img src={miniatura} alt="Imagem do resíduo" />
                <div className="cad-previa-acoes">
                  <button type="button" className="cad-link" onClick={() => inputArquivo.current?.click()}>Trocar imagem</button>
                  {previa && <button type="button" className="cad-link" onClick={removerImagem}>Descartar nova</button>}
                </div>
              </div>
            ) : (
              <button
                type="button"
                className={`cad-upload${arrastando ? ' arrastando' : ''}`}
                onClick={() => inputArquivo.current?.click()}
                onDragOver={(e) => { e.preventDefault(); setArrastando(true) }}
                onDragLeave={() => setArrastando(false)}
                onDrop={(e) => { e.preventDefault(); setArrastando(false); aceitarArquivo(e.dataTransfer.files[0]) }}
              >
                <span className="cad-upload-icone"><UploadCloud /></span>
                Arraste uma imagem ou clique para enviar
              </button>
            )}
            <input ref={inputArquivo} type="file" accept={TIPOS_IMAGEM.join(',')} hidden onChange={escolherImagem} />
          </div>

          {erro && <p className="error cad-erro" role="alert">{erro}</p>}

          <div className="cad-acoes">
            <button type="button" className="cad-btn" onClick={onClose} disabled={enviando}>Cancelar</button>
            <button type="submit" className="cad-btn primario" disabled={enviando}>
              {enviando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Publicar resíduo'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
