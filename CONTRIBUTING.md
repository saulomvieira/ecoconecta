# Guia de Contribuição — EcoConecta

Este documento estabelece as convenções de desenvolvimento e colaboração adotadas pela equipe do EcoConecta.

## 1. Organização das branches

A branch `main` representa a versão principal e estável do projeto.

Cada nova funcionalidade, correção ou melhoria deverá ser desenvolvida em uma branch separada.

### Padrões de nomenclatura

- `feature/nome-da-funcionalidade` — novas funcionalidades.
- `fix/nome-da-correcao` — correção de erros.
- `docs/nome-da-documentacao` — alterações na documentação.
- `refactor/nome-da-melhoria` — reorganização do código sem alterar seu comportamento.

Exemplos:

- `feature/cadastro-usuario`
- `feature/crud-residuos`
- `fix/validacao-email`

## 2. Padrão de commits

Utilizaremos mensagens de commit baseadas no padrão Conventional Commits.

**Formato:** `tipo: descrição da alteração`

Exemplos:

- `feat: implementa cadastro de usuarios`
- `fix: corrige validacao de email`
- `docs: atualiza README`
- `refactor: reorganiza servico de residuos`
- `test: adiciona testes de cadastro`

## 3. Pull Requests

Toda funcionalidade deverá ser integrada à `main` por meio de um Pull Request (PR).

Antes de solicitar a integração:

- Verificar se a funcionalidade atende aos requisitos.
- Garantir que o código compila.
- Executar os testes disponíveis.
- Descrever as alterações realizadas.
- Solicitar a revisão de outro integrante.

## 4. Revisão de código

Sempre que possível, um integrante diferente do autor deverá revisar o Pull Request.

A revisão deve verificar a clareza do código, o funcionamento da funcionalidade e o cumprimento dos requisitos.

## 5. Organização das tarefas

As atividades serão registradas e acompanhadas no Jira.

Cada tarefa deverá conter uma descrição, um responsável e critérios de conclusão.

Quando possível, branches, commits e Pull Requests deverão mencionar o identificador da tarefa correspondente.

## 6. Regras gerais

- Evitar commits diretamente na `main`.
- Não enviar senhas, tokens ou credenciais para o repositório.
- Não versionar arquivos gerados automaticamente ou configurações locais desnecessárias.
- Manter a documentação atualizada.
- Comunicar alterações que possam afetar o trabalho de outros integrantes.
