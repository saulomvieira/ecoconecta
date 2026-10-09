# ♻️ EcoConecta

O **EcoConecta** é uma aplicação que busca conectar pessoas que disponibilizam resíduos recicláveis ou compostáveis a coletores interessados, facilitando o reaproveitamento de materiais.

## Tecnologias utilizadas

**Backend**
- Java 21
- Spring Boot 4.1.1
- Spring Data JPA
- Maven
- PostgreSQL 17
- BCrypt para hash de senhas

**Frontend**
- React (em desenvolvimento)

## Pré-requisitos

Para executar o backend localmente, é necessário instalar:

- JDK 21
- PostgreSQL 17
- IntelliJ IDEA ou outra IDE compatível com Java
- Git

## Configuração do banco de dados

Cada integrante utilizará seu próprio banco PostgreSQL local.

1. Abra o pgAdmin 4.
2. Crie um banco de dados chamado `ecoconecta`.
3. Configure o PostgreSQL para utilizar a porta `5432`.
4. Guarde a senha do usuário `postgres`.

O arquivo `backend/src/main/resources/application.yaml` utiliza a seguinte configuração:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecoconecta
    username: postgres
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
```

**Importante:** a senha do PostgreSQL não deve ser adicionada diretamente ao código nem enviada ao GitHub.

## Executando o backend

1. Clone o repositório:

```bash
git clone https://github.com/saulomvieira/ecoconecta.git
```

2. Abra a pasta `backend` no IntelliJ IDEA.

3. Aguarde o Maven carregar as dependências.

4. Acesse **Run → Edit Configurations** e configure:

- Main class: `backend.BackendApplication`
- Java: 21
- Working directory: pasta `backend`
- Environment variables: `DB_PASSWORD=senha_local_do_postgresql`

5. Execute a aplicação pela configuração criada.

Quando o servidor iniciar corretamente, a API estará disponível em:

`http://localhost:8080`

O Hibernate criará ou atualizará as tabelas necessárias no banco local.

## Endpoints implementados

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/usuarios` | Cadastrar usuário |
| GET | `/usuarios` | Listar usuários |
| GET | `/usuarios/{id}` | Buscar usuário por ID |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| DELETE | `/usuarios/{id}` | Excluir usuário |

O cadastro possui validação de campos, verificação de e-mail duplicado e armazenamento de senhas com hash BCrypt.

**Atenção:** a autenticação e as permissões de acesso ainda estão em desenvolvimento. Os endpoints não devem ser expostos publicamente nesta fase.

## Trabalho em equipe

O desenvolvimento será realizado por meio de branches e Pull Requests.

1. Atualize a branch `main` antes de iniciar uma nova tarefa.
2. Crie uma branch para a funcionalidade que desenvolverá.
3. Faça commits com descrições claras.
4. Envie a branch para o GitHub.
5. Abra um Pull Request para integrar as alterações à `main`.

Evite modificar diretamente a branch `main`.

Cada integrante terá seu próprio banco de dados local. Portanto, os registros cadastrados em um computador não serão compartilhados automaticamente com os demais.

## Status do projeto

**Concluído:** estrutura inicial do backend, conexão com PostgreSQL, CRUD básico de usuários, validações e hash de senhas.

**Em desenvolvimento:** login, CRUD de resíduos, reservas e coletas, planos de assinatura, administração e frontend React.

---

Projeto acadêmico desenvolvido em equipe.
