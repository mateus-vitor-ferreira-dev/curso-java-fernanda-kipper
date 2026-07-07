-- A URL do S3 (~128 chars) não cabia em VARCHAR(100) e img_url era NOT NULL,
-- mas o evento pode não ter imagem. Aumenta o tamanho e torna a coluna opcional.
ALTER TABLE event ALTER COLUMN img_url TYPE VARCHAR(255);
ALTER TABLE event ALTER COLUMN img_url DROP NOT NULL;
