CREATE SEQUENCE IF NOT EXISTS utilizadores_id_seq;
ALTER TABLE utilizadores ALTER COLUMN id SET DEFAULT nextval('utilizadores_id_seq');
CREATE SEQUENCE IF NOT EXISTS menus_id_seq;
CREATE SEQUENCE IF NOT EXISTS morada_utilizador_id_seq;
CREATE SEQUENCE IF NOT EXISTS catalogo_entregas_id_seq;
CREATE SEQUENCE IF NOT EXISTS produto_id_seq;

INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Admin Tasca Eats', 'admin.restaurante@tascaeats.local', 'admin123'
WHERE NOT EXISTS (
    SELECT 1
    FROM utilizadores
    WHERE email = 'admin.restaurante@tascaeats.local'
);

INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Cliente Tasca', 'cliente@tascaeats.local', 'cliente123'
WHERE NOT EXISTS (
    SELECT 1
    FROM utilizadores
    WHERE email = 'cliente@tascaeats.local'
);

INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Entregador Tasca', 'courier@tascaeats.local', 'courier123'
WHERE NOT EXISTS (
    SELECT 1
    FROM utilizadores
    WHERE email = 'courier@tascaeats.local'
);

INSERT INTO restaurante_admin (id)
SELECT u.id
FROM utilizadores u
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM restaurante_admin ra
      WHERE ra.id = u.id
  );

INSERT INTO cliente (id)
SELECT u.id
FROM utilizadores u
WHERE u.email = 'cliente@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM cliente c
      WHERE c.id = u.id
  );

INSERT INTO entregador (id, available)
SELECT u.id, true
FROM utilizadores u
WHERE u.email = 'courier@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM entregador e
      WHERE e.id = u.id
  );

WITH seeded_menu_1 AS (
    INSERT INTO menus (id, nome)
  SELECT nextval('menus_id_seq'), 'Menu Tasca Central'
    WHERE NOT EXISTS (
        SELECT 1
        FROM menus
        WHERE nome = 'Menu Tasca Central'
    )
    RETURNING id
),
seeded_morada_1 AS (
    INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
  SELECT nextval('morada_utilizador_id_seq'), 'Rua da Tasca', 25, 'Loja A', 'Portugal', 'Lisboa', 'Lisboa', '1000-001', NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '123456789'
    )
    RETURNING id
),
seeded_catalogo_1 AS (
    INSERT INTO catalogo_entregas (id)
  SELECT nextval('catalogo_entregas_id_seq')
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '123456789'
    )
    RETURNING id
)
INSERT INTO restaurante (
    nome,
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
    menu_id
)
SELECT
    'Tasca Central',
    '123456789',
    'Franquia Tasca Eats',
    'ABERTO',
    'PORTUGUES',
    12,
    4.6,
    12.5,
    seeded_morada_1.id,
    seeded_catalogo_1.id,
    u.id,
    m.id
FROM seeded_morada_1
CROSS JOIN seeded_catalogo_1
JOIN utilizadores u
    ON u.email = 'admin.restaurante@tascaeats.local'
JOIN seeded_menu_1 m ON true;

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
JOIN restaurante r
    ON r.nif = '123456789'
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM restaurant_admin_restaurants rar
      WHERE rar.restaurant_admin_id = u.id
        AND rar.restaurant_id = r.id
  );

WITH seeded_menu_2 AS (
    INSERT INTO menus (id, nome)
  SELECT nextval('menus_id_seq'), 'Menu Sushi Zen'
    WHERE NOT EXISTS (
        SELECT 1
        FROM menus
        WHERE nome = 'Menu Sushi Zen'
    )
    RETURNING id
),
seeded_morada_2 AS (
    INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
  SELECT nextval('morada_utilizador_id_seq'), 'Avenida do Mar', 10, 'Piso 0', 'Portugal', 'Lisboa', 'Cascais', '2750-001', NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '987654321'
    )
    RETURNING id
),
seeded_catalogo_2 AS (
    INSERT INTO catalogo_entregas (id)
  SELECT nextval('catalogo_entregas_id_seq')
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '987654321'
    )
    RETURNING id
)
INSERT INTO restaurante (
    nome,
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
    menu_id
)
SELECT
    'Sushi Zen',
    '987654321',
    'Franquia Tasca Eats',
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
JOIN seeded_menu_2 m ON true;

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
JOIN restaurante r
    ON r.nif IN ('123456789', '987654321')
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM restaurant_admin_restaurants rar
      WHERE rar.restaurant_admin_id = u.id
        AND rar.restaurant_id = r.id
  );

INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
SELECT nextval('morada_utilizador_id_seq'), 'Rua do Cliente', 5, 'Apt 2B', 'Portugal', 'Lisboa', 'Lisboa', '1000-200', u.id
FROM utilizadores u
WHERE u.email = 'cliente@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM morada_utilizador mu
      WHERE mu.cliente_id = u.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Bitoque', 12.50, true, 'Bife grelhado servido com ovo estrelado, batatas fritas e arroz.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Tasca Central'
WHERE r.nif = '123456789'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Bitoque'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Super Bock 33cl', 2.00, true, 'Cerveja fresca e equilibrada, com sabor suave e refrescante.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Tasca Central'
WHERE r.nif = '123456789'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Super Bock 33cl'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Pastel de Nata', 1.50, true, 'Ícone da doçaria portuguesa, caracterizado por uma base de massa folhada estaladiça e um recheio cremoso à base de ovos, leite, açúcar, limão e canela', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Tasca Central'
WHERE r.nif = '123456789'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Pastel de Nata'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Combinado Sushi 15 peças', 22.00, true, 'Seleção do chefe com peças frescas de sushi.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Sushi Zen'
WHERE r.nif = '987654321'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Combinado Sushi 15 peças'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Chá Verde', 3.00, true, 'Chá verde quente.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Sushi Zen'
WHERE r.nif = '987654321'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Chá Verde'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Gyoza', 6.50, true, 'Deliciosos pastéis japoneses recheados, crocantes por fora e suculentos por dentro. Perfeitos para partilhar ou começar a refeição.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Sushi Zen'
WHERE r.nif = '987654321'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Gyoza'
        AND p.restaurante_id = r.id
  );

-- Additional seed data: more users, restaurants, menus and products

INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Cliente Dois', 'cliente2@tascaeats.local', 'cliente223'
WHERE NOT EXISTS (
    SELECT 1
    FROM utilizadores
    WHERE email = 'cliente2@tascaeats.local'
);

INSERT INTO utilizadores (id, nome, email, password)
SELECT nextval('utilizadores_id_seq'), 'Entregador Dois', 'courier2@tascaeats.local', 'courier223'
WHERE NOT EXISTS (
    SELECT 1
    FROM utilizadores
    WHERE email = 'courier2@tascaeats.local'
);

INSERT INTO cliente (id)
SELECT u.id
FROM utilizadores u
WHERE u.email = 'cliente2@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM cliente c
      WHERE c.id = u.id
  );

INSERT INTO entregador (id, available)
SELECT u.id, true
FROM utilizadores u
WHERE u.email = 'courier2@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM entregador e
      WHERE e.id = u.id
  );

INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
SELECT nextval('morada_utilizador_id_seq'), 'Travessa do Bairro', 12, 'R/c', 'Portugal', 'Porto', 'Porto', '4000-123', u.id
FROM utilizadores u
WHERE u.email = 'cliente2@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM morada_utilizador mu
      WHERE mu.cliente_id = u.id
  );

WITH seeded_menu_3 AS (
    INSERT INTO menus (id, nome)
  SELECT nextval('menus_id_seq'), 'Menu Pizzaria do Bairro'
    WHERE NOT EXISTS (
        SELECT 1
        FROM menus
        WHERE nome = 'Menu Pizzaria do Bairro'
    )
    RETURNING id
),
seeded_morada_3 AS (
    INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
  SELECT nextval('morada_utilizador_id_seq'), 'Largo da Pizza', 3, NULL, 'Portugal', 'Porto', 'Porto', '4100-001', NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '111222333'
    )
    RETURNING id
),
seeded_catalogo_3 AS (
    INSERT INTO catalogo_entregas (id)
  SELECT nextval('catalogo_entregas_id_seq')
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '111222333'
    )
    RETURNING id
)
INSERT INTO restaurante (
    nome,
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
    menu_id
)
SELECT
    'Pizzaria do Bairro',
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
JOIN seeded_menu_3 m ON true;

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
JOIN restaurante r
    ON r.nif = '111222333'
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM restaurant_admin_restaurants rar
      WHERE rar.restaurant_admin_id = u.id
        AND rar.restaurant_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Margherita', 8.50, true, 'Pizza clássica com tomate e mozzarella.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Pizzaria do Bairro'
WHERE r.nif = '111222333'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Margherita'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Portuguesa Pizza', 10.00, true, 'Pizza com fiambre, ovo e azeitonas.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Pizzaria do Bairro'
WHERE r.nif = '111222333'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Portuguesa Pizza'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Coca-Cola 33cl', 1.50, true, 'Refrigerante gaseificado de sabor doce e refrescante, servido bem fresco com gelo ou limão. Ideal para acompanhar refeições ou petiscos.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Pizzaria do Bairro'
WHERE r.nif = '111222333'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Coca-Cola 33cl'
        AND p.restaurante_id = r.id
  );

WITH seeded_menu_4 AS (
    INSERT INTO menus (id, nome)
  SELECT nextval('menus_id_seq'), 'Menu Verde'
    WHERE NOT EXISTS (
        SELECT 1
        FROM menus
        WHERE nome = 'Menu Verde'
    )
    RETURNING id
),
seeded_morada_4 AS (
    INSERT INTO morada_utilizador (id, rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
  SELECT nextval('morada_utilizador_id_seq'), 'Rua Vegetal', 7, NULL, 'Portugal', 'Coimbra', 'Coimbra', '3000-777', NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '444555666'
    )
    RETURNING id
),
seeded_catalogo_4 AS (
    INSERT INTO catalogo_entregas (id)
  SELECT nextval('catalogo_entregas_id_seq')
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '444555666'
    )
    RETURNING id
)
INSERT INTO restaurante (
    nome,
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
    menu_id
)
SELECT
    'Tasca Verde',
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
JOIN seeded_menu_4 m ON true;

INSERT INTO restaurant_admin_restaurants (restaurant_admin_id, restaurant_id)
SELECT u.id, r.id
FROM utilizadores u
JOIN restaurante r
    ON r.nif = '444555666'
WHERE u.email = 'admin.restaurante@tascaeats.local'
  AND NOT EXISTS (
      SELECT 1
      FROM restaurant_admin_restaurants rar
      WHERE rar.restaurant_admin_id = u.id
        AND rar.restaurant_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Hambúrguer Vegetariano', 7.50, true, 'Hambúrguer vegetariano com molho.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Verde'
WHERE r.nif = '444555666'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Hambúrguer Vegetariano'
        AND p.restaurante_id = r.id
  );

INSERT INTO produto (id, nome, preco, disponivel, descricao, apagado, restaurante_id, menu_id)
SELECT nextval('produto_id_seq'), 'Salada Mix', 5.00, true, 'Mistura fresca de alface, tomate, cenoura e outros vegetais da época. Leve, saudável e ideal como acompanhamento.', false, r.id, m.id
FROM restaurante r
JOIN menus m ON m.nome = 'Menu Verde'
WHERE r.nif = '444555666'
  AND NOT EXISTS (
      SELECT 1
      FROM produto p
      WHERE p.nome = 'Salada Mix'
        AND p.restaurante_id = r.id
  );
