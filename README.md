
# ♻️ EcoConecta

O **EcoConecta** é um projeto acadêmico desenvolvido para conectar pessoas que disponibilizam resíduos recicláveis ou compostáveis a usuários interessados em coletá-los, incentivando o reaproveitamento de materiais e a sustentabilidade.

A aplicação foi desenvolvida em equipe, com base nos requisitos e protótipos definidos no Figma.

## 🛠️ Tecnologias utilizadas

### Backend
- Java 21
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- Spring Security e JWT
- PostgreSQL 17
- Maven
- Bean Validation
- BCrypt

### Infraestrutura e ferramentas
- Docker e Docker Compose
- Git e GitHub
- Postman
- IntelliJ IDEA
- Jira
- pgAdmin

### Frontend
- React (previsto para uma etapa futura)

## 🏗️ Arquitetura

O backend utiliza uma arquitetura de **monólito modular**, organizada por funcionalidades e separação de responsabilidades.

Os módulos seguem, principalmente, a estrutura:

- **Controller:** recebe requisições HTTP e retorna respostas.
- **Service:** concentra as regras de negócio.
- **Repository:** realiza o acesso aos dados.
- **Model:** representa as entidades da aplicação.

A estrutura também inclui componentes de segurança, autenticação, tratamento de exceções e configurações compartilhadas.

## ✅ Funcionalidades implementadas

| Módulo | Funcionalidades |
|---|---|
| Usuários | Cadastro, consulta, atualização, exclusão e validações |
| Autenticação | Login, autenticação JWT e proteção de endpoints |
| Recuperação de senha | Solicitação e redefinição de senha |
| Resíduos | Cadastro, listagem, consulta, atualização e exclusão |
| Imagens de resíduos | Envio e consulta de imagens, com validações |
| Reservas | Criação, consulta, cancelamento e registro de coleta |
| Planos | Gerenciamento dos planos disponíveis |
| Assinaturas | Contratação e gerenciamento de assinaturas |
| Termos de uso | Gerenciamento de versões e registro de aceites |
| Administração | Operações administrativas e consultas de informações |

As operações possuem validações e regras de autorização de acordo com o usuário autenticado.

## 🔐 Segurança

A aplicação utiliza **Spring Security** e autenticação baseada em **JSON Web Token (JWT)**.

Entre os mecanismos implementados estão:

- Armazenamento de senhas utilizando BCrypt.
- Autenticação por token JWT.
- Controle de acesso por perfil de usuário.
- Proteção de endpoints administrativos.
- Verificação de permissões sobre recursos pertencentes aos usuários.
- Validação dos dados recebidos.
- Tratamento de exceções e respostas HTTP apropriadas.

## 🐳 Executando com Docker

### 1. Pré-requisitos

- Git
- Docker Desktop com Docker Compose

### 2. Clonar o repositório

```bash
git clone https://github.com/saulomvieira/ecoconecta.git
cd ecoconecta
```

### 3. Configurar variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto, contendo pelo menos:

```env
DB_PASSWORD=defina_uma_senha_local
JWT_SECRET=defina_uma_chave_secreta_forte
```

Utilize uma chave JWT forte e adequada à configuração da aplicação.

O arquivo `.env` não deve ser enviado ao GitHub.

Funcionalidades que utilizam envio de e-mails podem exigir configuração adicional de SMTP.

### 4. Iniciar a aplicação

Na raiz do repositório, execute:

```bash
docker compose up -d --build
```

O Docker Compose inicia o backend e o PostgreSQL.

### 5. Verificar os containers

```bash
docker compose ps
```

Para acompanhar a inicialização:

```bash
docker compose logs -f app
```

A API estará disponível em:

```text
http://localhost:8080
```

O PostgreSQL estará acessível localmente pela porta `5433`, conforme a configuração do Docker Compose.

### 6. Encerrar os containers

```bash
docker compose down
```

Esse comando encerra os serviços sem excluir automaticamente o volume de dados do PostgreSQL.

## 💻 Executando localmente com Maven

Também é possível executar o backend sem utilizar o container da aplicação.

Entre na pasta:

```bash
cd backend
```

Configure as variáveis de ambiente necessárias, especialmente `DB_PASSWORD` e `JWT_SECRET`, e mantenha uma instância do PostgreSQL acessível.

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação utiliza arquivos de configuração do Spring Boot e permite configurar a conexão com o banco por variáveis de ambiente.

## 🌐 API REST

O backend disponibiliza endpoints para as funcionalidades do EcoConecta.

Alguns exemplos de rotas:

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/usuarios` | Cadastrar usuário |
| GET | `/usuarios` | Listar usuários, conforme permissão |
| GET | `/usuarios/{id}` | Consultar usuário |
| PUT | `/usuarios/{id}` | Atualizar usuário |
| DELETE | `/usuarios/{id}` | Excluir usuário, conforme permissão |

Além dessas rotas, a aplicação possui endpoints para autenticação, resíduos, imagens, reservas, planos, assinaturas, termos de uso e administração.

**Observação:** parte dos endpoints exige autenticação por JWT e permissões específicas.

## 🗄️ Banco de dados

O projeto utiliza **PostgreSQL 17** para persistência dos dados.

O acesso ao banco é realizado por meio do Spring Data JPA e Hibernate.

Durante o desenvolvimento, a configuração `ddl-auto: update` permite que o Hibernate atualize o esquema das entidades mapeadas.

O Docker Compose utiliza um volume para persistir os dados do PostgreSQL entre reinicializações dos containers.

## 🖼️ Upload de imagens

O EcoConecta permite associar imagens aos resíduos cadastrados.

O backend realiza validações relacionadas ao tipo de arquivo, tamanho permitido e permissões do usuário.

No ambiente Docker, os arquivos podem ser armazenados no diretório:

```text
backend/uploads/residuos/
```

Esse diretório é ignorado pelo Git para evitar o versionamento dos arquivos enviados durante a utilização da aplicação.

## 🧪 Testes

Durante o desenvolvimento, os endpoints foram testados utilizando o **Postman**, contemplando operações de cadastro, consulta, atualização e exclusão, além de cenários de validação, autenticação, autorização e upload de imagens.

A aplicação também foi compilada e inicializada utilizando Docker Compose, com conexão ao PostgreSQL.

## 👥 Organização do desenvolvimento

O projeto foi desenvolvido em equipe, com divisão de responsabilidades entre os integrantes.

O versionamento foi realizado utilizando Git e GitHub, com branches para desenvolvimento das funcionalidades e integração do código.

Fluxo adotado:

```text
Branches de funcionalidades
          ↓
       develop
          ↓
         main
```

A branch `develop` foi utilizada para integração e a `main` para a versão consolidada do projeto.

## 🎓 Escopo acadêmico

O EcoConecta foi desenvolvido como atividade acadêmica, com foco na implementação do backend, persistência de dados, regras de negócio, autenticação e integração entre os módulos.

A interface frontend completa não faz parte desta entrega.

---

**EcoConecta — Conectando pessoas e promovendo o reaproveitamento de resíduos.**
