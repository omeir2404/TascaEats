-- TascaEats - P1 4.1 validation queries
-- Run this file in Postgres to answer the business validation questions from the enunciado.

-- Optional: quick schema check
-- SELECT table_name
-- FROM information_schema.tables
-- WHERE table_schema = 'public'
-- ORDER BY table_name;

-- ============================================================
-- Q1) Quais os restaurantes com maior volume de vendas em euros?
-- ============================================================
SELECT
  r.id AS restaurante_id,
  r.nome AS restaurante_nome,
  COUNT(p.id) AS total_pedidos,
  COALESCE(SUM(p.valor_total), 0) AS volume_vendas_eur
FROM restaurante r
LEFT JOIN pedidos p
  ON p.restaurante_id = r.id
  AND p.estado IN ('PAGO', 'A_PREPARAR', 'PRONTO_PARA_ENTREGA', 'ATRIBUIDO_ENTREGADOR', 'ENTREGUE')
GROUP BY r.id, r.nome
ORDER BY volume_vendas_eur DESC, total_pedidos DESC;

-- ============================================================
-- Q2) Morada do restaurante com mais vendas
-- ============================================================
WITH ranking_restaurantes AS (
  SELECT
    r.id,
    r.nome,
    COALESCE(SUM(p.valor_total), 0) AS vendas
  FROM restaurante r
  LEFT JOIN pedidos p
    ON p.restaurante_id = r.id
    AND p.estado IN ('PAGO', 'A_PREPARAR', 'PRONTO_PARA_ENTREGA', 'ATRIBUIDO_ENTREGADOR', 'ENTREGUE')
  GROUP BY r.id, r.nome
)
SELECT
  rr.id AS restaurante_id,
  rr.nome AS restaurante_nome,
  rr.vendas AS volume_vendas_eur,
  m.rua,
  m.numero,
  m.complemento,
  m.codigo_postal,
  m.cidade,
  m.distrito,
  m.pais
FROM ranking_restaurantes rr
JOIN restaurante r ON r.id = rr.id
LEFT JOIN morada_utilizador m ON m.id = r.morada_id
ORDER BY rr.vendas DESC
LIMIT 1;

-- ============================================================
-- Q2b) Morada do cliente com mais pedidos
-- ============================================================
WITH ranking_clientes AS (
  SELECT
    c.id,
    COUNT(p.id) AS total_pedidos
  FROM cliente c
  LEFT JOIN pedidos p ON p.cliente_id = c.id
  GROUP BY c.id
)
SELECT
  rc.id AS cliente_id,
  u.name AS cliente_nome,
  u.email AS cliente_email,
  rc.total_pedidos,
  m.rua,
  m.numero,
  m.complemento,
  m.codigo_postal,
  m.cidade,
  m.distrito,
  m.pais
FROM ranking_clientes rc
JOIN utilizadores u ON u.id = rc.id
LEFT JOIN cliente c ON c.id = rc.id
LEFT JOIN morada_utilizador m ON m.id = c.address_id
ORDER BY rc.total_pedidos DESC
LIMIT 1;

-- ============================================================
-- Q3) Em média, quantos pedidos um cliente realiza por mês?
-- ============================================================
WITH pedidos_por_cliente_mes AS (
  SELECT
    p.cliente_id,
    DATE_TRUNC('month', p.data)::date AS mes,
    COUNT(*) AS total_no_mes
  FROM pedidos p
  GROUP BY p.cliente_id, DATE_TRUNC('month', p.data)
)
SELECT
  cliente_id,
  ROUND(AVG(total_no_mes)::numeric, 2) AS media_pedidos_por_mes
FROM pedidos_por_cliente_mes
GROUP BY cliente_id
ORDER BY media_pedidos_por_mes DESC;

-- ============================================================
-- Q4) Quais os produtos mais vendidos da plataforma?
-- Nota: este projeto tende a usar tabela de relação pedido_produtos.
-- ============================================================
SELECT
  pr.id AS produto_id,
  pr.nome AS produto_nome,
  COUNT(*) AS vezes_vendido
FROM pedido_produtos pp
JOIN produto pr ON pr.id = pp.produto_id
GROUP BY pr.id, pr.nome
ORDER BY vezes_vendido DESC, pr.id;

-- ============================================================
-- Q5) Qual o método de pagamento mais utilizado pelos clientes?
-- (coluna discriminator: tipo_pagamento)
-- ============================================================
SELECT
  pg.tipo_pagamento,
  COUNT(*) AS total_utilizacoes
FROM pagamentos pg
GROUP BY pg.tipo_pagamento
ORDER BY total_utilizacoes DESC, pg.tipo_pagamento;

-- ============================================================
-- Q6) Quais os clientes registados mas sem nenhuma compra?
-- ============================================================
SELECT
  c.id AS cliente_id,
  u.name AS cliente_nome,
  u.email AS cliente_email
FROM cliente c
JOIN utilizadores u ON u.id = c.id
LEFT JOIN pedidos p ON p.cliente_id = c.id
WHERE p.id IS NULL
ORDER BY c.id;
