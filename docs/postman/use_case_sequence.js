/*
Paste this script in the Tests tab of a Postman request named "Run Use Case Sequence".
Required collection variables:
- baseUrl (default: http://localhost:8080)
Optional collection variables:
- adminPassword, clientPassword, driverPassword, userUpdatePassword
- seedAdminCount (default: 3)
- seedClientCount (default: 6)
- seedDriverCount (default: 5)
- seedRestaurantsPerAdmin (default: 2)
- seedProductsPerRestaurant (default: 5)
- seedOrdersPerClient (default: 1)
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

const adminCount = Math.max(1, intVar("seedAdminCount", 3));
const clientCount = Math.max(1, intVar("seedClientCount", 6));
const driverCount = Math.max(1, intVar("seedDriverCount", 5));
const restaurantsPerAdmin = Math.max(1, intVar("seedRestaurantsPerAdmin", 2));
const productsPerRestaurant = Math.max(1, intVar("seedProductsPerRestaurant", 5));
const ordersPerClient = intVar("seedOrdersPerClient", 1);
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

function emailFor(type, idx) {
  return `${type}.${seed}.${idx}@tasca.pt`;
}

function nifFor(idx) {
  const value = 100000000 + ((seed + idx) % 900000000);
  return String(value);
}

function clientAddress(idx) {
  return {
    rua: `Rua Cliente ${idx}`,
    numero: 10 + idx,
    complemento: "1oDto",
    codigoPostal: `10${String(idx % 90).padStart(2, "0")}-1${String((idx + 7) % 90).padStart(2, "0")}`,
    cidade: idx % 2 === 0 ? "Lisboa" : "Porto",
    distrito: idx % 2 === 0 ? "Lisboa" : "Porto",
    pais: "Portugal"
  };
}

function restaurantAddress(idx) {
  return {
    rua: `Rua Restaurante ${idx}`,
    numero: 30 + idx,
    complemento: "Loja",
    codigoPostal: `20${String(idx % 90).padStart(2, "0")}-2${String((idx + 3) % 90).padStart(2, "0")}`,
    cidade: idx % 2 === 0 ? "Lisboa" : "Coimbra",
    distrito: idx % 2 === 0 ? "Lisboa" : "Coimbra",
    pais: "Portugal"
  };
}

(async function run() {
  try {
    const admins = [];
    const clients = [];
    const drivers = [];
    const restaurants = [];
    const productsByRestaurant = {};
    const seededOrderIds = [];

    // B) Register users in bulk
    for (let i = 1; i <= adminCount; i += 1) {
      const email = emailFor("admin", i);
      const adminReg = await send("POST", "/api/register", {
        name: `Admin Seq ${i}`,
        email,
        password: adminPassword,
        type: "admin"
      }, 201);

      admins.push({
        id: adminReg.json && adminReg.json.id,
        email
      });
    }

    for (let i = 1; i <= clientCount; i += 1) {
      const email = emailFor("client", i);
      const clientReg = await send("POST", "/api/register", {
        name: `Client Seq ${i}`,
        email,
        password: clientPassword,
        type: "client",
        address: clientAddress(i)
      }, 201);

      clients.push({
        id: clientReg.json && clientReg.json.id,
        email
      });
    }

    for (let i = 1; i <= driverCount; i += 1) {
      const email = emailFor("driver", i);
      const driverReg = await send("POST", "/api/register", {
        name: `Driver Seq ${i}`,
        email,
        password: driverPassword,
        type: "driver"
      }, 201);

      drivers.push({
        id: driverReg.json && driverReg.json.id,
        email
      });
    }

    pm.collectionVariables.set("adminEmail", admins[0].email);
    pm.collectionVariables.set("clientEmail", clients[0].email);
    pm.collectionVariables.set("driverEmail", drivers[0].email);
    pm.collectionVariables.set("adminEmails", admins.map(a => a.email).join(","));
    pm.collectionVariables.set("clientEmails", clients.map(c => c.email).join(","));
    pm.collectionVariables.set("driverEmails", drivers.map(d => d.email).join(","));

    if (admins[0].id) {
      pm.collectionVariables.set("adminId", String(admins[0].id));
    }
    if (clients[0].id) {
      pm.collectionVariables.set("clientId", String(clients[0].id));
    }

    // E) Create/open restaurants in bulk
    let nifSeq = 1;
    for (let a = 0; a < admins.length; a += 1) {
      for (let r = 1; r <= restaurantsPerAdmin; r += 1) {
        const createRestaurant = await send("POST", `/api/restaurantes?adminEmail=${encodeURIComponent(admins[a].email)}`, {
          nome: `Tasca Seq ${seed}-${a + 1}-${r}`,
          nif: nifFor(nifSeq),
          morada: restaurantAddress(nifSeq)
        }, 201);

        nifSeq += 1;
        const restauranteId = createRestaurant.json && createRestaurant.json.id;
        pm.test("Restaurant id created", function () {
          pm.expect(restauranteId).to.exist;
        });

        const createMenu = await send("POST", "/api/menus", {
          nome: `Menu Seq ${seed}-${a + 1}-${r}`
        }, 201);

        const menuId = createMenu.json && createMenu.json.id;
        pm.test("Menu id created", function () {
          pm.expect(menuId).to.exist;
        });

        await send("PUT", `/api/restaurantes/${restauranteId}/menu/${menuId}`, {}, 200);

        restaurants.push({
          id: restauranteId,
          menuId,
          adminEmail: admins[a].email
        });

        await send("PATCH", `/api/restaurantes/${restauranteId}/abrir`, null, 200);
      }
    }

    pm.collectionVariables.set("restauranteId", String(restaurants[0].id));
    pm.collectionVariables.set("restauranteIds", restaurants.map(r => String(r.id)).join(","));

    // F/G) Create products in bulk for each restaurant
    for (let i = 0; i < restaurants.length; i += 1) {
      const restauranteId = restaurants[i].id;
      const menuId = restaurants[i].menuId;
      productsByRestaurant[restauranteId] = [];

      for (let p = 1; p <= productsPerRestaurant; p += 1) {
        const createProduct = await send("POST", `/api/menus/${menuId}/produtos`, {
          nome: `Produto ${i + 1}-${p}`,
          preco: Number((6 + p + (i * 0.5)).toFixed(2)),
          descricao: `Produto seed ${seed} restaurante ${i + 1}`
        }, 201);

        const produtoId = createProduct.json && createProduct.json.id;
        pm.test("Product id created", function () {
          pm.expect(produtoId).to.exist;
        });

        productsByRestaurant[restauranteId].push(produtoId);
      }
    }

    const firstRestaurantId = restaurants[0].id;
    const firstProductId = productsByRestaurant[firstRestaurantId][0];

    pm.collectionVariables.set("produtoId", String(firstProductId));
    pm.collectionVariables.set(
      "produtoIds",
      restaurants.flatMap(r => productsByRestaurant[r.id]).map(id => String(id)).join(",")
    );

    // Seed lightweight orders first so DB is populated before use-case flow
    if (ordersPerClient > 0) {
      for (let c = 0; c < clients.length; c += 1) {
        const currentClient = clients[c];

        for (let o = 0; o < ordersPerClient; o += 1) {
          const restaurant = restaurants[(c + o) % restaurants.length];
          const restaurantProducts = productsByRestaurant[restaurant.id];
          const selectedProducts = restaurantProducts.slice(0, Math.min(2, restaurantProducts.length));

          await send("POST", "/pedidos", {
            clienteId: currentClient.id,
            restauranteId: restaurant.id,
            produtosIds: selectedProducts
          }, [200, 201]);

          const byClient = await send("GET", `/pedidos?clienteId=${currentClient.id}`, null, 200);
          if (Array.isArray(byClient.json) && byClient.json.length > 0) {
            const latest = byClient.json[byClient.json.length - 1];
            if (latest && latest.id) {
              seededOrderIds.push(Number(latest.id));
            }
          }
        }
      }
    }

    // Build a richer simulation by pushing seeded orders into different states.
    const uniqueOrderIds = [...new Set(seededOrderIds)];
    const targetCount = seedDeliveredOrders + seedPreparingOrders + seedPaidOnlyOrders + seedCancelledOrders;
    const simulationOrderIds = uniqueOrderIds.slice(0, Math.min(targetCount, uniqueOrderIds.length));

    let cursor = 0;
    const pick = (n) => {
      const chunk = simulationOrderIds.slice(cursor, cursor + n);
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
          valorRecebido: 50.0
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

    pm.collectionVariables.set("seededPedidoIds", simulationOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosDelivered", deliveredOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosPreparing", preparingOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosPaid", paidOnlyOrderIds.join(","));
    pm.collectionVariables.set("seededPedidosCancelled", cancelledOrderIds.join(","));

    // A) Login
    await send("POST", "/api/login", {
      email: admins[0].email,
      password: adminPassword
    }, 200);

    // C) Verify users and update user
    const listUsers = await send("GET", "/api/Utilizadors", null, 200);
    const expectedMinUsers = adminCount + clientCount + driverCount;
    pm.test("Users list contains seeded users", function () {
      pm.expect(Array.isArray(listUsers.json)).to.eql(true);
      pm.expect(listUsers.json.length >= expectedMinUsers).to.eql(true);
    });

    const driverUser = Array.isArray(listUsers.json)
      ? listUsers.json.find(u => u && u.email === drivers[0].email)
      : null;

    if (driverUser && driverUser.id) {
      pm.collectionVariables.set("driverId", String(driverUser.id));
      await send("PUT", `/api/Utilizadors/${driverUser.id}`, {
        name: "Driver Seq 1",
        email: drivers[0].email,
        password: driverPassword,
        type: "driver",
        available: true
      }, [200, 409]);
    }

    if (clients[0].id) {
      await send("PUT", `/api/Utilizadors/${clients[0].id}`, {
        name: "Client Seq Updated",
        email: clients[0].email,
        password: userUpdatePassword,
        type: "client"
      }, [200, 409]);
    }

    // Keep update/toggle flow for one product to validate use cases
    await send("PUT", `/api/produtos/${firstProductId}`, {
      id: firstProductId,
      nome: "Bitoque Seq Updated",
      preco: 11.0,
      descricao: "Atualizado"
    }, 200);

    await send("PATCH", `/api/produtos/${firstProductId}/disponibilidade`, {
      disponivel: false
    }, 200);

    await send("PATCH", `/api/produtos/${firstProductId}/disponibilidade`, {
      disponivel: true
    }, 200);

    const listByRestaurant = await send("GET", `/api/produtos?restauranteId=${firstRestaurantId}`, null, 200);
    pm.test("Restaurant has seeded products", function () {
      pm.expect(Array.isArray(listByRestaurant.json)).to.eql(true);
      pm.expect(listByRestaurant.json.length >= productsPerRestaurant).to.eql(true);
    });

    // H) Create one full order flow (for first client)
    const clientId = clients[0].id;
    await send("POST", "/pedidos", {
      clienteId: clientId,
      restauranteId: firstRestaurantId,
      produtosIds: [firstProductId]
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
