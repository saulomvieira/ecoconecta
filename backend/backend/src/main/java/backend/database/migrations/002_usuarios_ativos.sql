-- Executar no PostgreSQL antes de iniciar esta versão se ddl-auto não estiver habilitado.
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
