# ♻️ EcoConecta

O **EcoConecta** é uma aplicação que busca conectar pessoas que disponibilizam resíduos recicláveis ou compostáveis a coletores interessados, facilitando o reaproveitamento de materiais.

O projeto está sendo desenvolvido em equipe como atividade acadêmica, seguindo as funcionalidades propostas no Figma.

## Tecnologias utilizadas

**Backend**
- Java 21
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- Maven
- PostgreSQL 17
- Bean Validation
- BCrypt para criptografia de senhas

**Frontend (futuramente)**
- React

**Ferramentas**
- IntelliJ IDEA
- Git e GitHub
- Postman
- Jira
- pgAdmin

## Arquitetura do projeto

O EcoConecta utiliza uma arquitetura de **monólito modular**, organizada por funcionalidades e separada em camadas.

Cada módulo possui quatro componentes principais:

- **Controller:** recebe e responde às requisições HTTP.
- **Service:** implementa as regras de negócio.
- **Repository:** realiza a comunicação com o banco de dados.
- **Model:** representa as entidades da aplicação.

### Estrutura de diretórios

```text
ecoconecta/
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── backend/
│   │       │       ├── usuarios/
│   │       │       │   ├── controller/
│   │       │       │   │   └── UsuarioController.java
│   │       │       │   ├── service/
│   │       │       │   │   └── UsuarioService.java
│   │       │       │   ├── repository/
│   │       │       │   │   └── UsuarioRepository.java
│   │       │       │   └── model/
│   │       │       │       └── Usuario.java
│   │       │       │
│   │       │       ├── residuos/
│   │       │       │   ├── controller/
│   │       │       │   │   └── ResiduoController.java
│   │       │       │   ├── service/
│   │       │       │   │   └── ResiduoService.java
│   │       │       │   ├── repository/
│   │       │       │   │   └── ResiduoRepository.java
│   │       │       │   └── model/
│   │       │       │       └── Residuo.java
│   │       │       │
│   │       │       ├── reservas/
│   │       │       │   ├── controller/
│   │       │       │   │   └── ReservaController.java
│   │       │       │   ├── service/
│   │       │       │   │   └── ReservaService.java
│   │       │       │   ├── repository/
│   │       │       │   │   └── ReservaRepository.java
│   │       │       │   └── model/
│   │       │       │       └── Reserva.java
│   │       │       │
│   │       │       ├── planos/
│   │       │       │   ├── controller/
│   │       │       │   │   └── PlanoController.java
│   │       │       │   ├── service/
│   │       │       │   │   └── PlanoService.java
│   │       │       │   ├── repository/
│   │       │       │   │   └── PlanoRepository.java
│   │       │       │   └── model/
│   │       │       │       └── Plano.java
│   │       │       │
│   │       │       ├── administracao/
│   │       │       │   ├── controller/
│   │       │       │   │   └── AdministracaoController.java
│   │       │       │   ├── service/
│   │       │       │   │   └── AdministracaoService.java
│   │       │       │   ├── repository/
│   │       │       │   │   └── AdministracaoRepository.java
│   │       │       │   └── model/
│   │       │       │       └── Administracao.java
│   │       │       │
│   │       │       ├── exception/
│   │       │       │   └── GlobalExceptionHandler.java
│   │       │       └── BackendApplication.java
│   │       │
│   │       └── resources/
│   │           └── application.yaml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── README.md
└── CONTRIBUTING.md
```

**Importante:** as classes dos módulos de resíduos, reservas, planos e administração **já foram criadas**, mas estão inicialmente sem a implementação dos CRUDs.

Cada integrante deverá desenvolver as funcionalidades dentro das classes existentes, respeitando a organização dos pacotes.

## Configuração do ambiente

### 1. Clonar o repositório

```bash
git clone https://github.com/saulomvieira/ecoconecta.git
```

Depois, abra a pasta `backend` no IntelliJ IDEA como projeto Maven.

### 2. Configurar o PostgreSQL

Cada integrante utilizará seu **próprio banco de dados local**, evitando conflitos durante o desenvolvimento.

Requisitos:
- PostgreSQL instalado e em execução.
- Banco de dados chamado `ecoconecta`.
- Usuário PostgreSQL configurado localmente.

Para criar o banco, utilize o pgAdmin ou execute:

```sql
CREATE DATABASE ecoconecta;
```

Não é necessário criar manualmente as tabelas já mapeadas como entidades JPA. Durante o desenvolvimento, o Hibernate utiliza `ddl-auto: update` para criar ou atualizar as tabelas.

### 3. Configurar a conexão

O arquivo `backend/src/main/resources/application.yaml` utiliza a seguinte configuração:

```yaml
spring:
  application:
    name: backend
  datasource:
    url: jdbc:postgresql://localhost:5432/ecoconecta
    username: postgres
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

Cada integrante deve configurar a variável de ambiente `DB_PASSWORD` com a senha do seu próprio PostgreSQL.

No IntelliJ IDEA:

1. Acesse **Run → Edit Configurations**.
2. Selecione a configuração de execução do backend.
3. Em **Environment variables**, adicione `DB_PASSWORD` com sua senha local.
4. Salve as alterações.

**Nunca envie senhas ou credenciais para o GitHub.**

### 4. Executar o backend

Execute a classe:

`BackendApplication.java`

Ou, com a variável `DB_PASSWORD` configurada no terminal, execute na pasta `backend`:

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

A API estará disponível em:

`http://localhost:8080`

## Situação do desenvolvimento

| Módulo | Situação |
|---|---|
| Usuários | CRUD implementado e testado |
| Resíduos | Classes criadas — CRUD pendente |
| Reservas | Classes criadas — CRUD pendente |
| Planos de assinatura | Classes criadas — CRUD pendente |
| Administração e termos | Classes criadas — funcionalidades pendentes |

### CRUD de usuários

O módulo de usuários já possui:

- Cadastro de usuários.
- Listagem de usuários.
- Consulta por ID.
- Atualização de usuários.
- Exclusão de usuários.
- Validação dos campos obrigatórios.
- Verificação de e-mails duplicados.
- Tratamento de erros HTTP 400, 404 e 409.
- Armazenamento de senhas com BCrypt.

**Endpoints disponíveis:**

| Método | Endpoint | Operação |
|---|---|---|
| POST | `/usuarios` | Cadastrar usuário |
| GET | `/usuarios` | Listar usuários |
| GET | `/usuarios/{id}` | Consultar usuário |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| DELETE | `/usuarios/{id}` | Excluir usuário |

## Divisão de responsabilidades

O desenvolvimento está dividido entre cinco integrantes:

| Módulo | Responsabilidade |
|---|---|
| Usuários | CRUD concluído |
| Resíduos | Implementação do CRUD de resíduos |
| Reservas | Implementação do CRUD de reservas e coletas |
| Planos | Implementação do CRUD de planos |
| Administração | CRUD de termos e operações administrativas |

As atividades estão organizadas no **Jira**, na Sprint 1 do projeto EcoConecta.

## Fluxo de trabalho com Git e GitHub

Cada integrante deverá trabalhar em sua própria branch, criada a partir da `main` atualizada.

**Branches sugeridas:**

```text
feat/residuos
feat/reservas
feat/planos
feat/administracao
```

### Procedimento

1. Atualizar a branch `main` antes de começar.
2. Criar a branch correspondente ao módulo.
3. Implementar as funcionalidades nas classes já existentes.
4. Testar os endpoints no Postman.
5. Realizar commit e push.
6. Abrir um Pull Request para a `main`.
7. Aguardar a revisão e integração das alterações.

**Não desenvolver diretamente na branch `main`.**

Como cada integrante possui um PostgreSQL local, os dados cadastrados em um computador não aparecem automaticamente nos bancos dos outros integrantes.

## Regras de desenvolvimento

- Manter a organização por funcionalidades.
- Utilizar Controller, Service, Repository e Model.
- Aplicar validações aos dados recebidos.
- Retornar códigos HTTP adequados.
- Evitar duplicação de regras de negócio.
- Respeitar os relacionamentos entre entidades.
- Não alterar o módulo de outro integrante sem combinar previamente.
- Não publicar senhas ou arquivos sensíveis.
- Testar as funcionalidades antes de abrir um Pull Request.

## Escopo atual

Nesta etapa, o objetivo é implementar os **CRUDs completos da aplicação**, conforme solicitado pelo professor e com base no Figma.

A implementação do frontend, autenticação e demais funcionalidades avançadas será realizada posteriormente.

---

**EcoConecta — Conectando pessoas e promovendo o reaproveitamento de resíduos.**
