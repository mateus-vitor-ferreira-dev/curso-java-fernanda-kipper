-- Seed de eventos para demo/apresentação (paginação)
-- Gera 48 eventos com datas FUTURAS (a partir de 2026-07-15), bem além da sexta 2026-07-10.
-- 48 eventos => 4 páginas com size=12 (ou mais páginas com size menor).
-- Cria endereço (cidade/UF) apenas para eventos PRESENCIAIS (remote = false).
-- Eventos remotos não têm endereço, igual à regra do createEvent no service.
-- Rodar no Postgres local (container eventostec_postgres, porta 5434), banco `eventostec`.
--
-- docker exec -i eventostec_postgres psql -U postgres -d eventostec < seed-eventos-demo.sql

WITH novos AS (
    INSERT INTO event (title, description, img_url, event_url, date, remote)
    SELECT
        (ARRAY['Java','Spring Boot','Cloud & AWS','DevOps',
               'Front-end','IA & Dados','Arquitetura','Carreira Dev'])[(n - 1) % 8 + 1]
            || ' Meetup #' || n                                                    AS title,
        'Palestra e networking — edição ' || n || '.'                              AS description,
        NULL                                                                       AS img_url,
        'https://eventos.tec/evento/' || n                                         AS event_url,
        -- começa em 2026-07-15 e adiciona 2 dias por evento (espaça no futuro)
        TIMESTAMP '2026-07-15 19:00:00' + (n * INTERVAL '2 days')                  AS date,
        (n % 3 = 0)                                                                AS remote
    FROM generate_series(1, 48) AS n
    RETURNING id, event_url, remote
)
INSERT INTO address (city, uf, event_id)
SELECT
    (ARRAY['São Paulo','Rio de Janeiro','Belo Horizonte','Curitiba',
           'Porto Alegre','Recife','Florianópolis','Salvador'])
        [(CAST(regexp_replace(event_url, '\D', '', 'g') AS INT) - 1) % 8 + 1],
    (ARRAY['SP','RJ','MG','PR','RS','PE','SC','BA'])
        [(CAST(regexp_replace(event_url, '\D', '', 'g') AS INT) - 1) % 8 + 1],
    id
FROM novos
WHERE NOT remote;   -- só eventos presenciais recebem endereço
