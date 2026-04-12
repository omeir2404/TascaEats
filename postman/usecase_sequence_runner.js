/*
Paste this script in the Tests tab of a Postman request named "Run Use Case Sequence".
Required collection variables:
- baseUrl (default: http://localhost:8080)
Optional collection variables:
- adminPassword, clientPassword, driverPassword, userUpdatePassword
- seedPaidOnlyOrders (default: 1)
- seedPreparingOrders (default: 1)
- seedDeliveredOrders (default: 2)
- seedCancelledOrders (default: 1)

This script runs a core end-to-end sequence:
1) Register admin/client/driver
2) Login admin
3) Create and open restaurant
4) Create and update product
5) Create order and fetch created order ID
6) Process payment
7) Start prep -> mark ready -> assign driver -> pickup -> final delivery
*/

const baseUrl = pm.collectionVariables.get("baseUrl") || "http://localhost:8080";

const seed = Date.now();
const adminEmail = `admin.${seed}@tasca.pt`;
const clientEmail = `client.${seed}@tasca.pt`;
const driverEmail = `driver.${seed}@tasca.pt`;

const adminPassword = pm.collectionVariables.get("adminPassword") || "pw123";
const clientPassword = pm.collectionVariables.get("clientPassword") || "pw123";
const driverPassword = pm.collectionVariables.get("driverPassword") || "pw123";
const userUpdatePassword = pm.collectionVariables.get("userUpdatePassword") || "pw456";

function intVar(name, fallback) {
  const raw = pm.collectionVariables.get(name);
  const parsed = Number.parseInt(raw, 10);
  if (!Number.isFinite(parsed) || parsed < 0) {
    return fallback;
  }
  return parsed;
}

const seedPaidOnlyOrders = Math.max(0, intVar("seedPaidOnlyOrders", 1));
const seedPreparingOrders = Math.max(0, intVar("seedPreparingOrders", 1));
const seedDeliveredOrders = Math.max(0, intVar("seedDeliveredOrders", 2));
const seedCancelledOrders = Math.max(0, intVar("seedCancelledOrders", 1));

function send(method, path, body, expectedStatus) {
  const hasBody = body !== undefined && body !== null;
  const url = `${baseUrl}${path}`;

  const buildReq = (variant) => {
    const req = { url, method };

    if (!hasBody) {
      req.header = "Accept: application/json";
      return req;
    }

    if (variant === 0) {
      req.header = "Content-Type: application/json\nAccept: application/json";
    } else if (variant === 1) {
      req.header = [
        { key: "Content-Type", value: "application/json" },
        { key: "Accept", value: "application/json" }
      ];
    } else {
      req.header = {
        "Content-Type": "application/json; charset=UTF-8",
        Accept: "application/json"
      };
    }

    req.body = {
      mode: "raw",
      raw: JSON.stringify(body),
      options: {
        raw: {
          language: "json"
        }
      }
    };

    return req;
  };

  return new Promise((resolve, reject) => {
    const tryRequest = (variant) => {
      pm.sendRequest(buildReq(variant), (err, res) => {
        if (err) {
          return reject(err);
        }

        if (hasBody && res.code === 415 && variant < 2) {
          return tryRequest(variant + 1);
        }

        const statusOk = Array.isArray(expectedStatus)
          ? expectedStatus.includes(res.code)
          : res.code === expectedStatus;

        pm.test(`${method} ${path} -> status ${Array.isArray(expectedStatus) ? expectedStatus.join("/") : expectedStatus}`, function () {
          pm.expect(statusOk, `Got ${res.code}. Body: ${res.text()}`).to.eql(true);
        });

        if (!statusOk) {
          return reject(new Error(`Unexpected status ${res.code} for ${method} ${path}`));
        }

        let json = null;
        try {
          json = res.json();
        } catch (_) {
          json = null;
        }

        resolve({ res, json });
      });
    };

    tryRequest(0);
  });
}

function clientAddress() {
  return {
    rua: "Rua Cliente",
    numero: 12,
    complemento: "1oDto",
    codigoPostal: "1000-100",
    cidade: "Lisboa",
    distrito: "Lisboa",
    pais: "Portugal"
  };
}

function restaurantAddress() {
  return {
    rua: "Rua Restaurante",
    numero: 30,
    complemento: "Loja",
    codigoPostal: "1000-200",
    cidade: "Lisboa",
    distrito: "Lisboa",
    pais: "Portugal"
  };
}

(async function run() {
  try {
    // B) Register users
    const adminReg = await send("POST", "/api/register", {
      name: "Admin Seq",
      email: adminEmail,
      password: adminPassword,
      type: "admin"
    }, 201);

    const clientReg = await send("POST", "/api/register", {
      name: "Client Seq",
      email: clientEmail,
      password: clientPassword,
      type: "client",
      address: clientAddress()
    }, 201);

    await send("POST", "/api/register", {
      name: "Driver Seq",
      email: driverEmail,
      password: driverPassword,
      type: "driver"
    }, 201);

    pm.collectionVariables.set("adminEmail", adminEmail);
    pm.collectionVariables.set("clientEmail", clientEmail);
    pm.collectionVariables.set("driverEmail", driverEmail);

    if (adminReg.json && adminReg.json.id) {
      pm.collectionVariables.set("adminId", String(adminReg.json.id));
    }
    if (clientReg.json && clientReg.json.id) {
      pm.collectionVariables.set("clientId", String(clientReg.json.id));
    }

    // A) Login
    await send("POST", "/api/login", {
      email: adminEmail,
      password: adminPassword
    }, 200);

    // C) Verify users and update user
    const listUsers = await send("GET", "/api/Utilizadors", null, 200);
    pm.test("Users list contains at least 3 users", function () {
      pm.expect(Array.isArray(listUsers.json)).to.eql(true);
      pm.expect(listUsers.json.length >= 3).to.eql(true);
    });

    const driverUser = Array.isArray(listUsers.json)
      ? listUsers.json.find(u => u && u.email === driverEmail)
      : null;

    if (driverUser && driverUser.id) {
      pm.collectionVariables.set("driverId", String(driverUser.id));
      await send("PUT", `/api/Utilizadors/${driverUser.id}`, {
        name: "Driver Seq",
        email: driverEmail,
        password: driverPassword,
        type: "driver",
        available: true
      }, [200, 409]);
    }

    if (clientReg.json && clientReg.json.id) {
      await send("PUT", `/api/Utilizadors/${clientReg.json.id}`, {
        name: "Client Seq Updated",
        email: clientEmail,
        password: userUpdatePassword,
        type: "client"
      }, [200, 409]);
    }

    // E) Create/open restaurant
    const createRestaurant = await send("POST", `/api/restaurantes?adminEmail=${encodeURIComponent(adminEmail)}`, {
      nome: `Tasca Seq ${seed}`,
      nif: `${String(seed).slice(-9)}`,
      morada: restaurantAddress()
    }, 201);

    const restauranteId = createRestaurant.json && createRestaurant.json.id;
    pm.test("Restaurant id created", function () {
      pm.expect(restauranteId).to.exist;
    });
    pm.collectionVariables.set("restauranteId", String(restauranteId));

    await send("PATCH", `/api/restaurantes/${restauranteId}/abrir`, null, 200);

    // F/G) Create/update/list product
    const createProduct = await send("POST", `/api/restaurantes/${restauranteId}/produtos`, {
      nome: "Bitoque Seq",
      preco: 10.5,
      descricao: "Bitoque para teste"
    }, 201);

    const produtoId = createProduct.json && createProduct.json.id;
    pm.test("Product id created", function () {
      pm.expect(produtoId).to.exist;
    });
    pm.collectionVariables.set("produtoId", String(produtoId));

    await send("PUT", `/api/produtos/${produtoId}`, {
      id: produtoId,
      nome: "Bitoque Seq Updated",
      preco: 11.0,
      descricao: "Atualizado"
    }, 200);

    await send("PATCH", `/api/produtos/${produtoId}/disponibilidade`, {
      disponivel: false
    }, 200);

    await send("PATCH", `/api/produtos/${produtoId}/disponibilidade`, {
      disponivel: true
    }, 200);

    const listByRestaurant = await send("GET", `/api/produtos?restauranteId=${restauranteId}`, null, 200);
    pm.test("Restaurant has at least one product", function () {
      pm.expect(Array.isArray(listByRestaurant.json)).to.eql(true);
      pm.expect(listByRestaurant.json.length >= 1).to.eql(true);
    });

    // H) Create base order
    const clientId = clientReg.json && clientReg.json.id;
    await send("POST", "/pedidos", {
      clienteId: clientId,
      restauranteId: restauranteId,
      produtosIds: [produtoId]
    }, [200, 201]);

    const pedidos = await send("GET", `/pedidos?clienteId=${clientId}`, null, 200);
    pm.test("Order list is not empty", function () {
      pm.expect(Array.isArray(pedidos.json)).to.eql(true);
      pm.expect(pedidos.json.length >= 1).to.eql(true);
    });

    const pedido = pedidos.json[pedidos.json.length - 1];
    const pedidoId = pedido && pedido.id;
    pm.test("Order id found", function () {
      pm.expect(pedidoId).to.exist;
    });
    pm.collectionVariables.set("pedidoId", String(pedidoId));

    // Seed additional orders for richer simulation states.
    const extraOrderIds = [];
    const extraOrderCount = seedPaidOnlyOrders + seedPreparingOrders + seedDeliveredOrders + seedCancelledOrders;

    for (let i = 0; i < extraOrderCount; i += 1) {
      await send("POST", "/pedidos", {
        clienteId: clientId,
        restauranteId: restauranteId,
        produtosIds: [produtoId]
      }, [200, 201]);

      const byClient = await send("GET", `/pedidos?clienteId=${clientId}`, null, 200);
      if (Array.isArray(byClient.json) && byClient.json.length > 0) {
        const latest = byClient.json[byClient.json.length - 1];
        if (latest && latest.id) {
          extraOrderIds.push(Number(latest.id));
        }
      }
    }

    const uniqueExtraOrderIds = [...new Set(extraOrderIds)];
    let cursor = 0;
    const pick = (n) => {
      const chunk = uniqueExtraOrderIds.slice(cursor, cursor + n);
      cursor += chunk.length;
      return chunk;
    };

    const deliveredOrderIds = pick(seedDeliveredOrders);
    const preparingOrderIds = pick(seedPreparingOrders);
    const paidOnlyOrderIds = pick(seedPaidOnlyOrders);
    const cancelledOrderIds = pick(seedCancelledOrders);

    for (let i = 0; i < cancelledOrderIds.length; i += 1) {
      await send("PATCH", `/pedidos/${cancelledOrderIds[i]}/cancelar`, null, 200);
    }

    const payOrder = async (orderId) => {
      await send("POST", "/pagamentos", {
        pedidoId: orderId,
        tipoPagamento: "DINHEIRO",
        detalhes: {
          valorRecebido: 20.0
        }
      }, 200);
    };

    for (let i = 0; i < paidOnlyOrderIds.length; i += 1) {
      await payOrder(paidOnlyOrderIds[i]);
    }

    for (let i = 0; i < preparingOrderIds.length; i += 1) {
      const orderId = preparingOrderIds[i];
      await payOrder(orderId);
      await send("PATCH", `/entregas/pedido/${orderId}/iniciar-preparacao`, null, 200);
    }

    for (let i = 0; i < deliveredOrderIds.length; i += 1) {
      const orderId = deliveredOrderIds[i];
      await payOrder(orderId);
      await send("PATCH", `/entregas/pedido/${orderId}/iniciar-preparacao`, null, 200);
      await send("PATCH", `/entregas/pedido/${orderId}/marcar-pronto`, null, 200);
      await send("POST", "/entregas/atribuir", { pedidoId: orderId }, 200);
      await send("PATCH", `/entregas/pedido/${orderId}/recolha`, null, 200);
      await send("PATCH", `/entregas/pedido/${orderId}/entrega-final`, null, 200);
    }

    pm.collectionVariables.set("seededPedidoIds", uniqueExtraOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosDelivered", deliveredOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosPreparing", preparingOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosPaid", paidOnlyOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosCancelled", cancelledOrderIds.join(","));

    // I) Payment
    await send("POST", "/pagamentos", {
      pedidoId: pedidoId,
      tipoPagamento: "DINHEIRO",
      detalhes: {
        valorRecebido: 20.0
      }
    }, 200);

    // N/P/Q/R) Delivery flow
    await send("PATCH", `/entregas/pedido/${pedidoId}/iniciar-preparacao`, null, 200);
    await send("PATCH", `/entregas/pedido/${pedidoId}/marcar-pronto`, null, 200);
    await send("POST", "/entregas/atribuir", { pedidoId: pedidoId }, 200);
    await send("PATCH", `/entregas/pedido/${pedidoId}/recolha`, null, 200);
    await send("PATCH", `/entregas/pedido/${pedidoId}/entrega-final`, null, 200);

    pm.test("Use case sequence completed", function () {
      pm.expect(true).to.eql(true);
    });
  } catch (e) {
    pm.test("Use case sequence failed", function () {
      pm.expect.fail(e.message || String(e));
    });
  }
})();
