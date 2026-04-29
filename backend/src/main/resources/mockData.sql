-- ==========================================
-- 1. MOCK RESTAURANTS
-- ==========================================
-- A highly-rated Portuguese place
INSERT INTO restaurante (id, nome, tipo_cozinha, estado, numero_avaliacoes, avaliacao_media, morada, preco_medio) 
VALUES (1, 'Tasca do Zé', 'PORTUGUESA', 'ABERTO', 150, 4.8, 'Rua Augusta, Lisboa', 15.00) ON CONFLICT DO NOTHING;

-- An expensive Italian place that is currently closed
INSERT INTO restaurante (id, nome, tipo_cozinha, estado, numero_avaliacoes, avaliacao_media, morada, preco_medio) 
VALUES (2, 'Bella Napoli', 'ITALIANA', 'FECHADO', 85, 4.2, 'Avenida da Liberdade, Lisboa', 35.50) ON CONFLICT DO NOTHING;

-- A popular Japanese sushi spot
INSERT INTO restaurante (id, nome, tipo_cozinha, estado, numero_avaliacoes, avaliacao_media, morada, preco_medio) 
VALUES (3, 'Sushi Zen', 'JAPONESA', 'ABERTO', 320, 4.9, 'Parque das Nações, Lisboa', 25.00) ON CONFLICT DO NOTHING;

-- A cheap Portuguese spot with fewer reviews
INSERT INTO restaurante (id, nome, tipo_cozinha, estado, numero_avaliacoes, avaliacao_media, morada, preco_medio) 
VALUES (4, 'Café Central', 'PORTUGUESA', 'ABERTO', 12, 3.5, 'Praça do Comércio, Lisboa', 8.50) ON CONFLICT DO NOTHING;


-- ==========================================
-- 2. MOCK PRODUCTS (Menus)
-- ==========================================
-- Menu for Tasca do Zé (Restaurant 1)
INSERT INTO produto (id, nome, preco, categoria, disponivel, popularidade, restaurante_id) 
VALUES (1, 'Bacalhau à Brás', 12.50, 'MAIN_COURSE', true, 4.9, 1) ON CONFLICT DO NOTHING;

INSERT INTO produto (id, nome, preco, categoria, disponivel, popularidade, restaurante_id) 
VALUES (2, 'Super Bock 33cl', 2.00, 'DRINK', true, 4.5, 1) ON CONFLICT DO NOTHING;

INSERT INTO produto (id, nome, preco, categoria, disponivel, popularidade, restaurante_id) 
VALUES (3, 'Pastel de Nata', 1.50, 'DESSERT', false, 5.0, 1) ON CONFLICT DO NOTHING; -- Out of stock test!

-- Menu for Bella Napoli (Restaurant 2)
INSERT INTO produto (id, nome, preco, categoria, disponivel, popularidade, restaurante_id) 
VALUES (4, 'Pizza Margherita', 14.00, 'MAIN_COURSE', true, 4.1, 2) ON CONFLICT DO NOTHING;

INSERT INTO produto (id, nome, preco, categoria, disponivel, popularidade, restaurante_id) 
VALUES (5, 'Tiramisu', 6.50, 'DESSERT', true, 4.8, 2) ON CONFLICT DO NOTHING;

-- Menu for Sushi Zen (Restaurant 3)
INSERT INTO produto (id, nome, preco, categoria, disponivel, popularidade, restaurante_id) 
VALUES (6, 'Combinado Salmon 15 peças', 22.00, 'MAIN_COURSE', true, 4.9, 3) ON CONFLICT DO NOTHING;

INSERT INTO produto (id, nome, preco, categoria, disponivel, popularidade, restaurante_id) 
VALUES (7, 'Chá Verde', 3.00, 'DRINK', true, 4.0, 3) ON CONFLICT DO NOTHING;

-- Adjust sequence generators so Hibernate doesn't crash when you try to save a new item from the UI later
ALTER SEQUENCE restaurante_seq RESTART WITH 100;
ALTER SEQUENCE produto_seq RESTART WITH 100;