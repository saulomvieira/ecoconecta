# EcoConecta — backend reorganizado

Este pacote usa **backend(5).zip** como base, reorganiza os arquivos Java para o caminho Maven correto `backend/src/main/java/backend` e incorpora recuperação de senha, upload de imagem e aceite obrigatório de termos do pacote anterior.

## Importante
- Faça um commit ou backup antes de substituir os arquivos.
- Extraia na raiz do repositório `ecoconecta`, de modo que exista somente `ecoconecta/backend/src/main/java/backend`. Não cole a pasta `backend` dentro de `src/main/java/backend`.
- A pasta antiga `src.main` que foi criada acidentalmente precisa ser removida do projeto **depois** de confirmar que a nova estrutura está correta; não a deixe dentro da árvore de fontes.
- Mantenha o `mvnw.cmd` e a pasta `.mvn` já existentes no seu repositório: o ZIP não inclui esses arquivos.
- Configure `DB_PASSWORD`, `JWT_SECRET`, e para envio de e-mails `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, `SMTP_FROM`, `FRONTEND_URL`.
- Para Docker, use o perfil `docker` ou forneça `DB_URL` apontando para o PostgreSQL no Docker; persista `UPLOAD_DIR` em volume.
- Confira se `database/migrations/002_usuarios_ativos.sql` foi aplicado no PostgreSQL. O arquivo não executa sozinho.
- Execute no PowerShell: `cd backend` e `./mvnw.cmd clean compile`.

## Limites de validação
A estrutura do ZIP, XML e presença dos arquivos foram verificados. A compilação Maven, testes de endpoints e integração com PostgreSQL/SMTP não foram executados neste ambiente. Pagamentos continuam simulados.
