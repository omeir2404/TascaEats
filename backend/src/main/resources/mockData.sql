CREATE SEQUENCE IF NOT EXISTS utilizadores_id_seq;
ALTER TABLE utilizadores
    ALTER COLUMN id SET DEFAULT nextval('utilizadores_id_seq');
CREATE SEQUENCE IF NOT EXISTS menus_id_seq;
CREATE SEQUENCE IF NOT EXISTS morada_utilizador_id_seq;
CREATE SEQUENCE IF NOT EXISTS catalogo_entregas_id_seq;
CREATE SEQUENCE IF NOT EXISTS produto_id_seq;

ALTER TABLE restaurante
    ALTER COLUMN menu_id DROP NOT NULL;
ALTER TABLE produto
    ALTER COLUMN restaurante_id DROP NOT NULL;

INSERT INTO menus (id, nome)
SELECT nextval('menus_id_seq'), 'Menu Tasca Central'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE nome = 'Menu Tasca Central');

INSERT INTO menus (id, nome)
SELECT nextval('menus_id_seq'), 'Menu Sushi Zen'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE nome = 'Menu Sushi Zen');

INSERT INTO menus (id, nome)
SELECT nextval('menus_id_seq'), 'Menu Pizzaria do Bairro'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE nome = 'Menu Pizzaria do Bairro');

INSERT INTO menus (id, nome)
SELECT nextval('menus_id_seq'), 'Menu Verde'
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE nome = 'Menu Verde');

UPDATE restaurante r
SET menu_id = m.id
FROM menus m
WHERE r.nif IN ('123456789', '123456780')
  AND m.nome = 'Menu Tasca Central'
  AND (r.menu_id IS NULL OR r.menu_id <> m.id);

UPDATE restaurante r
SET menu_id = m.id
FROM menus m
WHERE r.nif = '987654321'
  AND m.nome = 'Menu Sushi Zen'
  AND (r.menu_id IS NULL OR r.menu_id <> m.id);

UPDATE restaurante r
SET menu_id = m.id
FROM menus m
WHERE r.nif = '111222333'
  AND m.nome = 'Menu Pizzaria do Bairro'
  AND (r.menu_id IS NULL OR r.menu_id <> m.id);

UPDATE restaurante r
SET menu_id = m.id
FROM menus m
WHERE r.nif = '444555666'
  AND m.nome = 'Menu Verde'
  AND (r.menu_id IS NULL OR r.menu_id <> m.id);

UPDATE produto p
SET menu_id = r.menu_id
FROM restaurante r
WHERE p.restaurante_id = r.id
  AND p.menu_id IS NULL
  AND r.menu_id IS NOT NULL;

UPDATE produto
SET restaurante_id = NULL
WHERE menu_id IS NOT NULL;

-- =========================================================================
-- 1. FORÇAR IDs 1 e 2 PARA OS ENTREGADORES (Sincronização com Microserviço)
-- =========================================================================
INSERT INTO utilizadores (id, nome, email, password)
SELECT 1, 'Entregador Tasca', 'courier@tascaeats.local', 'courier123'
WHERE NOT EXISTS (SELECT 1 FROM utilizadores WHERE email = 'courier@tascaeats.local');

INSERT INTO utilizadores (id, nome, email, password)
SELECT 2, 'Entregador Dois', 'courier2@tascaeats.local', 'courier223'
WHERE NOT EXISTS (SELECT 1 FROM utilizadores WHERE email = 'courier2@tascaeats.local');

-- Avançar a sequência para evitar colisões com utilizadores já existentes.
SELECT setval('utilizadores_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM utilizadores), 2), true);

-- =========================================================================
-- 2. INSERIR RESTANTES UTILIZADORES COM IDs DINÂMICOS
-- =========================================================================
INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Admin Tasca Eats', 'admin.restaurante@tascaeats.local', 'admin123'
WHERE NOT EXISTS (SELECT 1 FROM utilizadores WHERE email = 'admin.restaurante@tascaeats.local');

INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Cliente Tasca', 'cliente@tascaeats.local', 'cliente123'
WHERE NOT EXISTS (SELECT 1 FROM utilizadores WHERE email = 'cliente@tascaeats.local');

INSERT INTO restaurante_admin (id)
SELECT u.id
FROM utilizadores u
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM restaurante_admin ra
                  WHERE ra.id = u.id);

INSERT INTO cliente (id)
SELECT u.id
FROM utilizadores u
WHERE u.email = 'cliente@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM cliente c
                  WHERE c.id = u.id);

INSERT INTO entregador (id, available)
SELECT u.id, true
FROM utilizadores u
WHERE u.email = 'courier@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM entregador e
                  WHERE e.id = u.id);

WITH seeded_menu_1 AS (
    INSERT INTO menus (id, nome)
        SELECT nextval('menus_id_seq'), 'Menu Tasca Central'
        WHERE NOT EXISTS (SELECT 1
                          FROM menus
                          WHERE nome = 'Menu Tasca Central')
        RETURNING id),
     seeded_morada_1 AS (
         INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
             SELECT nextval('morada_utilizador_id_seq'),
                    'Rua da Tasca',
                    25,
                    'Loja A',
                    'Portugal',
                    'Lisboa',
                    'Lisboa',
                    '1000-001',
                    NULL
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '123456789')
             RETURNING id),
     seeded_catalogo_1 AS (
         INSERT INTO catalogo_entregas (id)
             SELECT nextval('catalogo_entregas_id_seq')
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '123456789')
             RETURNING id)
INSERT INTO restaurante (
    nome,
    nif,
    franquia,
    estado,
    tipo_restaurante,
    numero_avaliacoes,
    avaliacao_media,
    preco_medio,
    apagado,
    morada_id,
    catalogo_entregas_id,
    administrador_id,
    menu_id
)
SELECT
    'Tasca Central',
    '123456789',
    'Tasca Central',
    'ABERTO',
    'PORTUGUES',
    12,
    4.6,
    12.5,
    false,
    seeded_morada_1.id,
    seeded_catalogo_1.id,
    u.id,
    m.id
FROM seeded_morada_1
         CROSS JOIN seeded_catalogo_1
         JOIN utilizadores u
              ON u.email = 'admin.restaurante@tascaeats.local'
         JOIN menus m ON m.nome = 'Menu Tasca Central';

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
         JOIN restaurante r
              ON r.nif = '123456789'
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM restaurant_admin_restaurants rar
                  WHERE rar.restaurant_admin_id = u.id
                    AND rar.restaurant_id = r.id);

WITH seeded_morada_franquia_tasca AS (
    INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
        SELECT nextval('morada_utilizador_id_seq'),
               'Rua das Francesinhas',
               18,
               'Loja B',
               'Portugal',
               'Porto',
               'Porto',
               '4000-002',
               NULL
        WHERE NOT EXISTS (SELECT 1
                          FROM restaurante
                          WHERE nif = '123456780')
        RETURNING id),
     seeded_catalogo_franquia_tasca AS (
         INSERT INTO catalogo_entregas (id)
             SELECT nextval('catalogo_entregas_id_seq')
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '123456780')
             RETURNING id)
INSERT
INTO restaurante (nome,
                  nif,
                  franquia,
                  estado,
                  tipo_restaurante,
                  numero_avaliacoes,
                  avaliacao_media,
                  preco_medio,
                  apagado,
                  morada_id,
                  catalogo_entregas_id,
                  administrador_id,
                  menu_id)
SELECT 'Tasca Central Porto',
       '123456780',
       'Tasca Central',
       'ABERTO',
       'PORTUGUES',
       3,
       4.5,
       12.5,
       false,
       seeded_morada_franquia_tasca.id,
       seeded_catalogo_franquia_tasca.id,
       u.id,
       m.id
FROM seeded_morada_franquia_tasca
         CROSS JOIN seeded_catalogo_franquia_tasca
         JOIN utilizadores u
              ON u.email = 'admin.restaurante@tascaeats.local'
         JOIN menus m ON m.nome = 'Menu Tasca Central';

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
         JOIN restaurante r
              ON r.nif = '123456780'
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM restaurant_admin_restaurants rar
                  WHERE rar.restaurant_admin_id = u.id
                    AND rar.restaurant_id = r.id);

WITH seeded_menu_2 AS (
    INSERT INTO menus (id, nome)
        SELECT nextval('menus_id_seq'), 'Menu Sushi Zen'
        WHERE NOT EXISTS (SELECT 1
                          FROM menus
                          WHERE nome = 'Menu Sushi Zen')
        RETURNING id),
     seeded_morada_2 AS (
         INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
             SELECT nextval('morada_utilizador_id_seq'),
                    'Avenida do Mar',
                    10,
                    'Piso 0',
                    'Portugal',
                    'Lisboa',
                    'Cascais',
                    '2750-001',
                    NULL
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '987654321')
             RETURNING id),
     seeded_catalogo_2 AS (
         INSERT INTO catalogo_entregas (id)
             SELECT nextval('catalogo_entregas_id_seq')
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '987654321')
             RETURNING id)
INSERT
INTO restaurante (nome,
                  nif,
                  franquia,
                  estado,
                  tipo_restaurante,
                  numero_avaliacoes,
                  avaliacao_media,
                  preco_medio,
                  morada_id,
                  catalogo_entregas_id,
                  administrador_id,
                  menu_id)
SELECT 'Sushi Zen',
       '987654321',
       'Sushi Zen',
       'ABERTO',
       'JAPONES',
       8,
       4.8,
       18.9,
       seeded_morada_2.id,
       seeded_catalogo_2.id,
       u.id,
       m.id
FROM seeded_morada_2
         CROSS JOIN seeded_catalogo_2
         JOIN utilizadores u
              ON u.email = 'admin.restaurante@tascaeats.local'
         JOIN menus m ON m.nome = 'Menu Sushi Zen';

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
         JOIN restaurante r
              ON r.nif IN ('123456789', '123456780', '987654321')
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM restaurant_admin_restaurants rar
                  WHERE rar.restaurant_admin_id = u.id
                    AND rar.restaurant_id = r.id);

INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
SELECT nextval('morada_utilizador_id_seq'),
       'Rua do Cliente',
       5,
       'Apt 2B',
       'Portugal',
       'Lisboa',
       'Lisboa',
       '1000-200',
       u.id
FROM utilizadores u
WHERE u.email = 'cliente@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM morada_utilizador mu
                  WHERE mu.cliente_id = u.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Bitoque',
       12.50,
       true,
       'Bife grelhado servido com ovo estrelado, batatas fritas e arroz.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Tasca Central'
WHERE r.nif = '123456789'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Bitoque'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Super Bock 33cl',
       2.00,
       true,
       'Cerveja fresca e equilibrada, com sabor suave e refrescante.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Tasca Central'
WHERE r.nif = '123456789'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Super Bock 33cl'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Pastel de Nata',
       1.50,
       true,
       'Ícone da doçaria portuguesa, caracterizado por uma base de massa folhada estaladiça e um recheio cremoso à base de ovos, leite, açúcar, limão e canela',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Tasca Central'
WHERE r.nif = '123456789'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Pastel de Nata'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Combinado Sushi 15 peças',
       22.00,
       true,
       'Seleção do chefe com peças frescas de sushi.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Sushi Zen'
WHERE r.nif = '987654321'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Combinado Sushi 15 peças'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Chá Verde',
       3.00,
       true,
       'Chá verde quente.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Sushi Zen'
WHERE r.nif = '987654321'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Chá Verde'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Gyoza',
       6.50,
       true,
       'Deliciosos pastéis japoneses recheados, crocantes por fora e suculentos por dentro. Perfeitos para partilhar ou começar a refeição.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Sushi Zen'
WHERE r.nif = '987654321'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Gyoza'
                    AND p.menu_id = m.id);

-- Additional seed data: more users, restaurants, menus and products

INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Cliente Dois', 'cliente2@tascaeats.local', 'cliente223'
WHERE NOT EXISTS (SELECT 1
                  FROM utilizadores
                  WHERE email = 'cliente2@tascaeats.local');



INSERT INTO cliente (id)
SELECT u.id
FROM utilizadores u
WHERE u.email = 'cliente2@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM cliente c
                  WHERE c.id = u.id);

INSERT INTO entregador (id, available)
SELECT u.id, true
FROM utilizadores u
WHERE u.email = 'courier2@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM entregador e
                  WHERE e.id = u.id);

INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
SELECT nextval('morada_utilizador_id_seq'),
       'Travessa do Bairro',
       12,
       'R/c',
       'Portugal',
       'Porto',
       'Porto',
       '4000-123',
       u.id
FROM utilizadores u
WHERE u.email = 'cliente2@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM morada_utilizador mu
                  WHERE mu.cliente_id = u.id);

WITH seeded_menu_3 AS (
    INSERT INTO menus (id, nome)
        SELECT nextval('menus_id_seq'), 'Menu Pizzaria do Bairro'
        WHERE NOT EXISTS (SELECT 1
                          FROM menus
                          WHERE nome = 'Menu Pizzaria do Bairro')
        RETURNING id),
     seeded_morada_3 AS (
         INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
             SELECT nextval('morada_utilizador_id_seq'),
                    'Largo da Pizza',
                    3,
                    NULL,
                    'Portugal',
                    'Porto',
                    'Porto',
                    '4100-001',
                    NULL
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '111222333')
             RETURNING id),
     seeded_catalogo_3 AS (
         INSERT INTO catalogo_entregas (id)
             SELECT nextval('catalogo_entregas_id_seq')
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '111222333')
             RETURNING id)
INSERT
INTO restaurante (nome,
                  nif,
                  franquia,
                  estado,
                  tipo_restaurante,
                  numero_avaliacoes,
                  avaliacao_media,
                  preco_medio,
                  morada_id,
                  catalogo_entregas_id,
                  administrador_id,
                  menu_id)
SELECT 'Pizzaria do Bairro',
       '111222333',
       'Franquia Local',
       'ABERTO',
       'ITALIANO',
       20,
       4.4,
       11.0,
       seeded_morada_3.id,
       seeded_catalogo_3.id,
       u.id,
       m.id
FROM seeded_morada_3
         CROSS JOIN seeded_catalogo_3
         JOIN utilizadores u
              ON u.email = 'admin.restaurante@tascaeats.local'
         JOIN menus m ON m.nome = 'Menu Pizzaria do Bairro';

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
         JOIN restaurante r
              ON r.nif = '111222333'
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM restaurant_admin_restaurants rar
                  WHERE rar.restaurant_admin_id = u.id
                    AND rar.restaurant_id = r.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Margherita',
       8.50,
       true,
       'Pizza clássica com tomate e mozzarella.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Pizzaria do Bairro'
WHERE r.nif = '111222333'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Margherita'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Portuguesa Pizza',
       10.00,
       true,
       'Pizza com fiambre, ovo e azeitonas.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Pizzaria do Bairro'
WHERE r.nif = '111222333'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Portuguesa Pizza'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Coca-Cola 33cl',
       1.50,
       true,
       'Refrigerante.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Pizzaria do Bairro'
WHERE r.nif = '111222333'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Coca-Cola 33cl'
                    AND p.menu_id = m.id);

WITH seeded_menu_4 AS (
    INSERT INTO menus (id, nome)
        SELECT nextval('menus_id_seq'), 'Menu Verde'
        WHERE NOT EXISTS (SELECT 1
                          FROM menus
                          WHERE nome = 'Menu Verde')
        RETURNING id),
     seeded_morada_4 AS (
         INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
             SELECT nextval('morada_utilizador_id_seq'),
                    'Rua Vegetal',
                    7,
                    NULL,
                    'Portugal',
                    'Coimbra',
                    'Coimbra',
                    '3000-777',
                    NULL
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '444555666')
             RETURNING id),
     seeded_catalogo_4 AS (
         INSERT INTO catalogo_entregas (id)
             SELECT nextval('catalogo_entregas_id_seq')
             WHERE NOT EXISTS (SELECT 1
                               FROM restaurante
                               WHERE nif = '444555666')
             RETURNING id)
INSERT
INTO restaurante (nome,
                  nif,
                  franquia,
                  estado,
                  tipo_restaurante,
                  numero_avaliacoes,
                  avaliacao_media,
                  preco_medio,
                  morada_id,
                  catalogo_entregas_id,
                  administrador_id,
                  menu_id)
SELECT 'Tasca Verde',
       '444555666',
       'Franquia Verde',
       'ABERTO',
       'VEGETARIANA',
       5,
       4.7,
       9.5,
       seeded_morada_4.id,
       seeded_catalogo_4.id,
       u.id,
       m.id
FROM seeded_morada_4
         CROSS JOIN seeded_catalogo_4
         JOIN utilizadores u
              ON u.email = 'admin.restaurante@tascaeats.local'
         JOIN menus m ON m.nome = 'Menu Verde';

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
         JOIN restaurante r
              ON r.nif = '444555666'
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (SELECT 1
                  FROM restaurant_admin_restaurants rar
                  WHERE rar.restaurant_admin_id = u.id
                    AND rar.restaurant_id = r.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Burger Veg',
       7.50,
       true,
       'Hambúrguer vegetal com molho.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Verde'
WHERE r.nif = '444555666'
  AND NOT EXISTS (SELECT 1
                  FROM produto p
                  WHERE p.nome = 'Burger Veg'
                    AND p.menu_id = m.id);

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'),
       'Salada Mix',
       5.00,
       true,
       'Salada mista fresca.',
       false,
       NULL,
       m.id
FROM restaurante r
         JOIN menus m ON m.nome = 'Menu Verde'
WHERE r.nif = '444555666'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Salada Mix'
        AND p.menu_id = m.id
  );

-- Seed: orders for cliente@tascaeats.local.
-- Kept idempotent because the Docker Postgres volume persists between runs and
-- spring.sql.init.mode=always executes this file on every backend startup.
WITH client AS (
  SELECT u.id AS cliente_id
  FROM utilizadores u
  WHERE u.email = 'cliente@tascaeats.local')
INSERT
INTO pedidos (data, hora, valor_total, estado, cliente_id, tipo_pagamento_solicitado)
SELECT (CURRENT_DATE - INTERVAL '2 days')::date,
       (CURRENT_TIME - INTERVAL '2 hours')::time,
       15.50,
       'ENTREGUE',
       c.cliente_id,
       'MBWAY'
FROM client c
WHERE NOT EXISTS (SELECT 1
                  FROM pedidos p
                  WHERE p.cliente_id = c.cliente_id
                    AND p.valor_total = 15.50
                    AND p.estado = 'ENTREGUE'
                    AND p.tipo_pagamento_solicitado = 'MBWAY');

WITH client AS (
  SELECT u.id AS cliente_id
  FROM utilizadores u
  WHERE u.email = 'cliente@tascaeats.local')
INSERT
INTO pedidos (data, hora, valor_total, estado, cliente_id, tipo_pagamento_solicitado)
SELECT (CURRENT_DATE - INTERVAL '2 days')::date,
       (CURRENT_TIME - INTERVAL '2 hours')::time,
       15.50,
       'PAGO',
       c.cliente_id,
       'MBWAY'
FROM client c
WHERE NOT EXISTS (SELECT 1
                  FROM pedidos p
                  WHERE p.cliente_id = c.cliente_id
                    AND p.valor_total = 15.50
                    AND p.estado = 'PAGO'
                    AND p.tipo_pagamento_solicitado = 'MBWAY');

WITH client AS (
  SELECT u.id AS cliente_id
  FROM utilizadores u
  WHERE u.email = 'cliente@tascaeats.local')
INSERT
INTO pedidos (data, hora, valor_total, estado, cliente_id, tipo_pagamento_solicitado)
SELECT (CURRENT_DATE - INTERVAL '1 day')::date,
       (CURRENT_TIME - INTERVAL '5 hours')::time,
       30.00,
       'ENTREGUE',
       c.cliente_id,
       'CARTAO'
FROM client c
WHERE NOT EXISTS (SELECT 1
                  FROM pedidos p
                  WHERE p.cliente_id = c.cliente_id
                    AND p.valor_total = 30.00
                    AND p.estado = 'ENTREGUE'
                    AND p.tipo_pagamento_solicitado = 'CARTAO');

WITH client AS (
  SELECT u.id AS cliente_id
  FROM utilizadores u
  WHERE u.email = 'cliente@tascaeats.local'),
     seed_pedidos AS (SELECT p.id, p.valor_total, p.estado, p.tipo_pagamento_solicitado
                      FROM pedidos p
                      WHERE p.cliente_id = (SELECT cliente_id FROM client)
                        AND (
                          (p.valor_total = 15.50 AND p.tipo_pagamento_solicitado = 'MBWAY' AND
                           p.estado IN ('ENTREGUE', 'PAGO'))
                              OR
                          (p.valor_total = 30.00 AND p.tipo_pagamento_solicitado = 'CARTAO' AND p.estado = 'ENTREGUE')
                          )),
     seed_produtos AS (SELECT sp.id AS pedido_id, prod.id AS produto_id
                       FROM seed_pedidos sp
                                JOIN restaurante r ON (
                           (sp.valor_total = 15.50 AND r.nif = '123456789')
                               OR (sp.valor_total = 30.00 AND r.nif = '987654321')
                           )
                                JOIN produto prod ON prod.menu_id = r.menu_id
                       WHERE (sp.valor_total = 15.50 AND prod.nome IN ('Bitoque', 'Super Bock 33cl'))
                          OR (sp.valor_total = 30.00 AND prod.nome IN ('Combinado Sushi 15 peças', 'Chá Verde')))
INSERT
INTO pedido_produtos (pedido_id, produto_id)
SELECT sp.pedido_id, sp.produto_id
FROM seed_produtos sp
WHERE NOT EXISTS (SELECT 1
                  FROM pedido_produtos pp
                  WHERE pp.pedido_id = sp.pedido_id
                    AND pp.produto_id = sp.produto_id);

WITH client AS (
  SELECT u.id AS cliente_id
  FROM utilizadores u
  WHERE u.email = 'cliente@tascaeats.local'
)
INSERT INTO pedido_restaurantes (pedido_id, restaurante_id)
SELECT p.id, r.id
FROM pedidos p
JOIN pedido_produtos pp ON pp.pedido_id = p.id
JOIN produto prod ON prod.id = pp.produto_id
JOIN restaurante r ON r.menu_id = prod.menu_id
WHERE p.cliente_id = (SELECT cliente_id FROM client)
  AND r.nif IN ('123456789', '987654321')
  AND NOT EXISTS (SELECT 1
                  FROM pedido_restaurantes pr
                  WHERE pr.pedido_id = p.id
                    AND pr.restaurante_id = r.id)
GROUP BY p.id, r.id;

SELECT setval('utilizadores_id_seq', (SELECT COALESCE(MAX(id), 1) FROM utilizadores), true);
SELECT setval('morada_utilizador_id_seq', COALESCE((SELECT MAX(id) FROM morada_utilizador), 0) + 1, false);
