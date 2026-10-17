BEGIN;

ALTER TABLE assinaturas
DROP CONSTRAINT IF EXISTS assinaturas_status_check;

ALTER TABLE assinaturas
ADD CONSTRAINT assinaturas_status_check
CHECK (status IN ('ATIVA', 'INADIMPLENTE', 'CANCELADA', 'EXPIRADA'));

COMMIT;
