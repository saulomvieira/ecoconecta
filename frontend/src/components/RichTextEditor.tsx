import { EditorContent, useEditor, useEditorState } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { useEffect } from 'react'

interface Props {
  value: string
  onChange: (html: string) => void
  disabled?: boolean
}

export function RichTextEditor({ value, onChange, disabled }: Props) {
  const editor = useEditor({
    // StarterKit v3 já inclui negrito, itálico, sublinhado e títulos.
    extensions: [StarterKit.configure({ heading: { levels: [1, 2] } })],
    content: value,
    editable: !disabled,
    onUpdate: ({ editor }) => onChange(editor.isEmpty ? '' : editor.getHTML()),
  })

  // Sincroniza quando o conteúdo é trocado de fora (ex.: carregar versão atual).
  useEffect(() => {
    if (editor && value !== (editor.isEmpty ? '' : editor.getHTML())) {
      editor.commands.setContent(value)
    }
  }, [editor, value])

  useEffect(() => {
    editor?.setEditable(!disabled)
  }, [editor, disabled])

  const ativo = useEditorState({
    editor,
    selector: ({ editor }) => ({
      bold: editor?.isActive('bold') ?? false,
      italic: editor?.isActive('italic') ?? false,
      underline: editor?.isActive('underline') ?? false,
      h1: editor?.isActive('heading', { level: 1 }) ?? false,
      h2: editor?.isActive('heading', { level: 2 }) ?? false,
      p: editor?.isActive('paragraph') ?? false,
    }),
  })

  if (!editor || !ativo) return null
  const chain = () => editor.chain().focus()

  return (
    <div className="editor">
      <div className="editor-toolbar" role="toolbar" aria-label="Formatação">
        <button type="button" className={ativo.bold ? 'on' : ''} onClick={() => chain().toggleBold().run()}>
          <b>B</b>
        </button>
        <button type="button" className={ativo.italic ? 'on' : ''} onClick={() => chain().toggleItalic().run()}>
          <i>I</i>
        </button>
        <button type="button" className={ativo.underline ? 'on' : ''} onClick={() => chain().toggleUnderline().run()}>
          <u>U</u>
        </button>
        <span className="sep" />
        <button type="button" className={ativo.h1 ? 'on' : ''} onClick={() => chain().toggleHeading({ level: 1 }).run()}>
          Título 1
        </button>
        <button type="button" className={ativo.h2 ? 'on' : ''} onClick={() => chain().toggleHeading({ level: 2 }).run()}>
          Título 2
        </button>
        <button type="button" className={ativo.p ? 'on' : ''} onClick={() => chain().setParagraph().run()}>
          Parágrafo
        </button>
      </div>
      <EditorContent editor={editor} className="editor-content" />
    </div>
  )
}
