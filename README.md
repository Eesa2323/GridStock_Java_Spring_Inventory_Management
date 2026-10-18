# GridStock

GridStock is a smart warehouse inventory and picker routing system. The Spring Boot
backend exposes normalized JPA entities for warehouses, bin locations, inventory,
orders, and order items, plus a Dijkstra-based path optimization engine that
calculates the optimal picking route for any order. The UI is served from Spring Boot
at `/`.

## Run locally

Install Java 17+ and Maven, then run:

```bash
mvn spring-boot:run
```

Open http://localhost:8080. The default profile uses an in-memory H2 database for a
zero-setup demo. The H2 console is available at `/h2-console` (JDBC URL
`jdbc:h2:mem:gridstock`, user `SA`). To use a persistent database, replace the
datasource properties in `application.properties` with your database URL, username,
and password.

On startup, `DataSeeder` populates the empty database with a 5x5 warehouse grid
(25 bin locations), 10 inventory items, and 3 sample orders.

## Architecture

- **Entities** (`com.gridstock.entity`): `Warehouse`, `BinLocation`, `Inventory`,
  `Order`, `OrderItem` — normalized JPA relationships.
- **Path optimization** (`com.gridstock.dsa`): `DijkstraAlgorithm` finds shortest
  paths over a grid graph; `findOptimalRoute` solves the picking-order TSP variant
  with a greedy nearest-target strategy. `PathOptimizationService` maps the result
  onto bin locations and order items.
- **Stock movement** is transactional: picking an order item deducts inventory and
  marks the item picked; when every item in an order is picked the order is
  automatically advanced to `READY_TO_SHIP`.

## API

### Inventory

- `GET /api/inventory` — all inventory items.
- `GET /api/inventory/low-stock` — items where `quantity <= minThreshold`.
- `GET /api/inventory/{id}` — a single item by ID.
- `GET /api/inventory/sku/{sku}` — a single item by SKU.
- `POST /api/inventory` — create an item.
- `PUT /api/inventory/{id}` — update an item.
- `PATCH /api/inventory/{id}/quantity?change=N` — adjust quantity (rejects negatives).
- `POST /api/inventory/{id}/restock?quantity=N` — add stock.
- `DELETE /api/inventory/{id}` — delete an item.

### Orders

- `GET /api/orders` — all orders (with full order-item details).
- `GET /api/orders/pending` — orders still needing picking.
- `GET /api/orders/{id}` — a single order by ID.
- `GET /api/orders/number/{orderNumber}` — a single order by order number.
- `POST /api/orders` — create an order (`{ "orderNumber", "customerName", "status" }`).
- `POST /api/orders/{orderId}/items` — add an item to an order
  (`{ "inventoryId": 1, "quantity": 2 }`).
- `PUT /api/orders/{id}` — update an order.
- `PATCH /api/orders/{id}/status?status=PICKING` — update order status.
- `POST /api/orders/{orderId}/items/{itemId}/pick?quantity=N` — pick an item:
  deducts `quantity` from the order item and its inventory, marks the item picked,
  and advances the order to `READY_TO_SHIP` once fully picked.
- `DELETE /api/orders/{id}` — delete an order.

### Warehouses

- `GET /api/warehouses` — all warehouses (includes bin locations and inventory flags).
- `GET /api/warehouses/{id}` — a single warehouse by ID.
- `POST /api/warehouses` — create a warehouse.
- `PUT /api/warehouses/{id}` — update a warehouse.
- `DELETE /api/warehouses/{id}` — delete a warehouse.

### Path optimization

- `GET /api/path-optimization/route/{orderId}` — the optimal picking route for an
  order, as a list of `PathStep`s (walkway steps and picking points) with total
  distance and step count. Returns an empty route when the order has no items.
- `GET /api/path-optimization/route/by-number/{orderNumber}` — same lookup by order
  number.

## Frontend

The dashboard (`/`) shows the warehouse floor, the pending order queue, an inventory
table with low-stock indicators, a route visualization with per-item pick buttons,
an All Orders page, and a Create Order form. All state is fetched from the REST API
above; there is no build step.
