ALTER TABLE obra
  ADD COLUMN auditavel_id BIGINT NULL,
  ADD CONSTRAINT uk_obra_auditavel UNIQUE (auditavel_id),
  ADD CONSTRAINT fk_obra_auditavel FOREIGN KEY (auditavel_id) REFERENCES auditavel(id);

DO $$
DECLARE
  obra_atual RECORD;
  auditavel_novo BIGINT;
BEGIN
  FOR obra_atual IN SELECT id FROM obra ORDER BY id LOOP
    INSERT INTO auditavel (excluido) VALUES (false) RETURNING id INTO auditavel_novo;
    UPDATE obra SET auditavel_id = auditavel_novo WHERE id = obra_atual.id;
  END LOOP;
END $$;

ALTER TABLE obra ALTER COLUMN auditavel_id SET NOT NULL;
ALTER TABLE obra ALTER COLUMN responsavel_id DROP NOT NULL;
