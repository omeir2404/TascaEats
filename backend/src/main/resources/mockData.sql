INSERT INTO utilizadores (nome, email, password)
SELECT 'Admin Tasca Eats', 'admin.restaurante@tascaeats.local', 'admin123'
WHERE NOT EXISTS (
    SELECT 1
    FROM utilizadores
    WHERE email = 'admin.restaurante@tascaeats.local'
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

INSERT INTO menus (nome)
SELECT 'Menu Tasca Central'
WHERE NOT EXISTS (
    SELECT 1
    FROM menus
    WHERE nome = 'Menu Tasca Central'
);

WITH seeded_morada AS (
    INSERT INTO morada_utilizador (rua, numero, complemento, pais, distrito, cidade, codigo_postal, cliente_id)
    SELECT 'Rua da Tasca', 25, 'Loja A', 'Portugal', 'Lisboa', 'Lisboa', '1000-001', NULL
    WHERE NOT EXISTS (
        SELECT 1
        FROM restaurante
        WHERE nif = '123456789'
    )
    RETURNING id
),
seeded_catalogo AS (
    INSERT INTO catalogo_entregas (id)
    SELECT nextval(pg_get_serial_sequence('catalogo_entregas', 'id'))
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
    'OUTRO',
    0,
    0.0,
    0.0,
    seeded_morada.id,
    seeded_catalogo.id,
    u.id,
    m.id
FROM seeded_morada
CROSS JOIN seeded_catalogo
JOIN utilizadores u
    ON u.email = 'admin.restaurante@tascaeats.local'
JOIN menus m
    ON m.nome = 'Menu Tasca Central';

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
