# db_init.py
import os
from sqlalchemy import insert, text
from data.database.db_connection import engine, DB_PATH
from data.database.tables import (
    metadata,
    restaurante, categoria, restaurante_categoria,
    combo, usuario, cliente, tarjeta_cliente, repartidor,
    pedido, detalle_pedido
)
from services.hash_service import hash_password

print(f"Inicializando BD en: {DB_PATH}")
os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)

if os.path.exists(DB_PATH):
    os.remove(DB_PATH)
    print("BD anterior eliminada.")

metadata.create_all(engine)

# ── Restaurantes reales en Costa Rica ───────────────────────────────
# Todos dentro de la provincia de Heredia y zonas cercanas
# (cedula, nombre, direccion, imagen_url, latitud, longitud)
RESTAURANTES_SEED = [
    (
        "3-101-000001",
        "McDonald's San Pedro",
        "Mall San Pedro, Montes de Oca, San José",
        "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=600&fit=crop",
        9.9283,
        -84.0507
    ),
    (
        "3-101-000002",
        "KFC Heredia Centro",
        "Avenida 4, Heredia Centro",
        "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=600&fit=crop",
        9.9990,
        -84.1178
    ),
    (
        "3-101-000003",
        "Burger King Escazú",
        "Avenida Escazú, San Rafael de Escazú",
        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&fit=crop",
        9.9180,
        -84.1390
    ),
    (
        "3-101-000004",
        "Taco Bell Curridabat",
        "Centro Comercial Terramall, Curridabat",
        "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=600&fit=crop",
        9.9144,
        -84.0343
    ),
    (
        "3-101-000005",
        "Subway Heredia",
        "Paseo de las Flores, Heredia",
        "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=600&fit=crop",
        9.9980,
        -84.1165
    ),
    (
        "3-101-000006",
        "Popeyes Alajuela",
        "City Mall Alajuela, Alajuela Centro",
        "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=600&fit=crop",
        10.0162,
        -84.2116
    ),
    (
        "3-101-000007",
        "Pizza Hut San José",
        "Avenida Central, San José Centro",
        "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&fit=crop",
        9.9325,
        -84.0788
    ),
    (
        "3-101-000008",
        "Domino's Sabana",
        "La Sabana Sur, San José",
        "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600&fit=crop",
        9.9345,
        -84.1043
    ),
    (
        "3-101-000009",
        "Johnny Rockets Multiplaza",
        "Multiplaza Escazú, Escazú",
        "https://images.unsplash.com/photo-1550547660-d9450f859349?w=600&fit=crop",
        9.9418,
        -84.1534
    ),
    (
        "3-101-000010",
        "Quiznos Escazú",
        "Avenida Escazú, Escazú",
        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=600&fit=crop",
        9.9185,
        -84.1410
    ),
]

# ── Categorías ────────────────────────────────────────────────────────
CATEGORIAS_SEED = [
    "hamburguesas",
    "pollo",
    "mexicana",
    "sandwiches",
    "pizza",
    "italiana",
]

# ── Combos por restaurante (9 por restaurante = números 1–9) ──────────
COMBOS_POR_RESTAURANTE = {
    "McDonald's San Pedro": [
        ("Big Mac Combo",          "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Cuarto de Libra",        "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("McNuggets 10 Piezas",    "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("McPollo Deluxe",         "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Doble Cuarto de Libra",  "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Big Tasty Bacon",        "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
        ("McWrap Crispy",          "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Triple Cheeseburger",    "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
        ("Mega McCombo Familiar",  "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
    ],
    "KFC Heredia Centro": [
        ("Combo 2 Piezas",         "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Combo 3 Piezas",         "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Box Meal Personal",      "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Kentucky Sandwich",      "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Twister Original",       "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Bucket 6 Piezas",        "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Bucket 10 Piezas",       "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
        ("Combo Familiar",         "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Mega Bucket 14 Piezas",  "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
    ],
    "Burger King Escazú": [
        ("Whopper",                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Doble Whopper",          "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Whopper Jr",             "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Chicken Royale",         "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Bacon King",             "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Stacker Triple",         "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
        ("King Nuggets 9 Piezas",  "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
        ("Extreme Cheese",         "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Mega King Combo",        "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
    ],
    "Taco Bell Curridabat": [
        ("Crunchy Taco",           "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
        ("Supreme Taco",           "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Burrito Supreme",        "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Quesadilla de Pollo",    "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Nachos BellGrande",      "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Crunchwrap Supreme",     "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Chalupa Box",            "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Combo Fiesta 3 Tacos",   "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
        ("Family Pack Deluxe",     "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
    ],
    "Subway Heredia": [
        ("Italian BMT",            "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Pollo Teriyaki",         "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Atún Clásico",           "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Subway Melt",            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Steak & Cheese",         "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Veggie Delight",         "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
        ("Sub Costillas BBQ",      "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Sub Pollo Chipotle",     "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Combo Footlong",         "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
    ],
    "Popeyes Alajuela": [
        ("Chicken Sandwich Clásico","https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Combo 2 Piezas",         "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Combo 3 Piezas",         "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Tenders 5 Piezas",       "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Tenders 8 Piezas",       "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Bucket Familiar 12",     "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
        ("Spicy Sandwich Combo",   "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Mix Chicken Box",        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Mega Bucket 16 Piezas",  "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
    ],
    "Pizza Hut San José": [
        ("Personal Pepperoni",     "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&fit=crop"),
        ("Suprema Mediana",        "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
        ("Pizza de Carnes",        "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Hawaiana",               "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Vegetariana",            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Cheese Lovers",          "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Pizza Grande Especial",  "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Pizza Familiar",         "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Mega Fiesta 2 Pizzas",   "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
    ],
    "Domino's Sabana": [
        ("Pepperoni Clásica",      "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
        ("Deluxe",                 "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&fit=crop"),
        ("Extravaganzza",          "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Hawaiana",               "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Chicken BBQ",            "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Veggie",                 "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Domino's Especial",      "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Pizza Familiar",         "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Mega Combo 2 Pizzas",    "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
    ],
    "Johnny Rockets Multiplaza": [
        ("Original Burger",        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Smoke House",            "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Rocket Double",          "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Bacon Cheddar",          "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Chicken Club",           "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Crispy Chicken",         "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
        ("Route 66",               "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Rocket Shake Combo",     "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Ultimate Rocket",        "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
    ],
    "Quiznos Escazú": [
        ("Classic Italian",        "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400&fit=crop"),
        ("Turkey Ranch & Swiss",   "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&fit=crop"),
        ("Chicken Carbonara",      "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&fit=crop"),
        ("Honey Bacon Club",       "https://images.unsplash.com/photo-1513639776629-7b61b0ac49cb?w=400&fit=crop"),
        ("Steakhouse Beef",        "https://images.unsplash.com/photo-1527477396000-e27163b481c2?w=400&fit=crop"),
        ("Veggie Guacamole",       "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&fit=crop"),
        ("Combo Regular",          "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&fit=crop"),
        ("Combo Grande",           "https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&fit=crop"),
        ("Mega Combo Footlong",    "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&fit=crop"),
    ],
}

# Una categoría principal por restaurante (la tabla admite solo una por ahora
# via el seed; para múltiples habría que insertar más filas)
CATEGORIA_POR_RESTAURANTE = {
    "McDonald's San Pedro":    "hamburguesas",
    "KFC Heredia Centro":      "pollo",
    "Burger King Escazú":      "hamburguesas",
    "Taco Bell Curridabat":    "mexicana",
    "Subway Heredia":          "sandwiches",
    "Popeyes Alajuela":        "pollo",
    "Pizza Hut San José":      "pizza",
    "Domino's Sabana":         "pizza",
    "Johnny Rockets Multiplaza":"hamburguesas",
    "Quiznos Escazú":          "sandwiches",
}

with engine.begin() as conn:
    conn.execute(text("PRAGMA foreign_keys = ON"))

    # ── Categorías ────────────────────────────────────────────────
    cat_ids = {}
    for nombre_cat in CATEGORIAS_SEED:
        result = conn.execute(insert(categoria).values(NOMBRE=nombre_cat))
        cat_ids[nombre_cat] = result.inserted_primary_key[0]

    # ── Restaurantes + combos + categoría ─────────────────────────
    for ced, nombre, dir_, img, lat, lng in RESTAURANTES_SEED:
        res = conn.execute(insert(restaurante).values(
            CEDULA_JURIDICA=ced,
            NOMBRE=nombre,
            DIRECCION=dir_,
            IMAGEN_URL=img,
            LATITUD=lat,
            LONGITUD=lng,
        ))
        rest_id = res.inserted_primary_key[0]

        # Categoría principal
        cat_nombre = CATEGORIA_POR_RESTAURANTE[nombre]
        conn.execute(insert(restaurante_categoria).values(
            RESTAURANTE_ID=rest_id,
            CATEGORIA_ID=cat_ids[cat_nombre]
        ))

        # Combos (tupla nombre, imagen)
        for i, (nombre_combo, img_combo) in enumerate(COMBOS_POR_RESTAURANTE[nombre], start=1):
            precio = 3000 + (i * 1000)
            conn.execute(insert(combo).values(
                RESTAURANTE_ID=rest_id,
                NUMERO_COMBO=i,
                NOMBRE=nombre_combo,
                PRECIO=precio,
                IMAGEN_URL=img_combo,
            ))

    # ── Admin ─────────────────────────────────────────────────────────────
    res = conn.execute(insert(usuario).values(
        EMAIL="admin@cletaeats.com",
        PASSWORD_HASH=hash_password("admin123"),
        ROL="ADMIN",
        ESTADO=1
    ))

    # ── Cliente 1: Juan ───────────────────────────────────────────
    res = conn.execute(insert(usuario).values(
        EMAIL="cliente@test.com",
        PASSWORD_HASH=hash_password("123456"),
        ROL="CLIENTE",
        ESTADO=1
    ))
    u_cliente_id = res.inserted_primary_key[0]
    res = conn.execute(insert(cliente).values(
        USUARIO_ID=u_cliente_id,
        CEDULA="1-0001-0001",
        NOMBRE="Juan Cliente",
        DIRECCION="Heredia Centro, 100m norte del parque",
        TELEFONO="8888-0001"
    ))
    cliente_id = res.inserted_primary_key[0]

    conn.execute(insert(tarjeta_cliente).values(
        CLIENTE_ID=cliente_id,
        NUMERO="4111111111111111",
        ALIAS="Visa Principal",
        FECHA_VENCIMIENTO="12/27",
        CVV="123",
        ES_PRINCIPAL=1
    ))

    # ── Cliente 2: María ──────────────────────────────────────────
    res = conn.execute(insert(usuario).values(
        EMAIL="maria@test.com",
        PASSWORD_HASH=hash_password("123456"),
        ROL="CLIENTE",
        ESTADO=1
    ))
    u_maria_id = res.inserted_primary_key[0]
    res = conn.execute(insert(cliente).values(
        USUARIO_ID=u_maria_id,
        CEDULA="1-0003-0003",
        NOMBRE="María González",
        DIRECCION="Santo Domingo de Heredia, frente al parque",
        TELEFONO="8888-0003"
    ))
    cliente2_id = res.inserted_primary_key[0]

    conn.execute(insert(tarjeta_cliente).values(
        CLIENTE_ID=cliente2_id,
        NUMERO="5500005555555559",
        ALIAS="Mastercard Personal",
        FECHA_VENCIMIENTO="08/26",
        CVV="456",
        ES_PRINCIPAL=1
    ))

    # ── Repartidor 1: Pedro ───────────────────────────────────────
    res = conn.execute(insert(usuario).values(
        EMAIL="repartidor@test.com",
        PASSWORD_HASH=hash_password("123456"),
        ROL="REPARTIDOR",
        ESTADO=1
    ))
    u_rep_id = res.inserted_primary_key[0]
    res = conn.execute(insert(repartidor).values(
        USUARIO_ID=u_rep_id,
        CEDULA="1-0002-0002",
        NOMBRE="Pedro Repartidor",
        CORREO="repartidor@test.com",
        DIRECCION="Heredia, San Francisco",
        TELEFONO="8888-0002",
        TARJETA="4111111111111112",
        DISPONIBLE=1,
        KM_RECORRIDOS_DIARIOS=12.5,
        AMONESTACIONES=0,
        RATING=4.7
    ))
    rep_id = res.inserted_primary_key[0]

    # ── Repartidor 2: Ana ─────────────────────────────────────────
    res = conn.execute(insert(usuario).values(
        EMAIL="ana@test.com",
        PASSWORD_HASH=hash_password("123456"),
        ROL="REPARTIDOR",
        ESTADO=1
    ))
    u_ana_id = res.inserted_primary_key[0]
    res = conn.execute(insert(repartidor).values(
        USUARIO_ID=u_ana_id,
        CEDULA="1-0004-0004",
        NOMBRE="Ana Repartidora",
        CORREO="ana@test.com",
        DIRECCION="Alajuela, La Guácima",
        TELEFONO="8888-0004",
        TARJETA="4111111111111113",
        DISPONIBLE=1,
        KM_RECORRIDOS_DIARIOS=8.0,
        AMONESTACIONES=0,
        RATING=4.9
    ))
    rep2_id = res.inserted_primary_key[0]

    # ── Historial de pedidos ──────────────────────────────────────
    # Estado: 0=pendiente, 1=preparando, 2=en camino, 3=entregado, 4=cancelado

    # Pedido 1 — Juan en McDonald's, entregado por Pedro
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente_id,
        RESTAURANTE_ID=1,
        REPARTIDOR_ID=rep_id,
        FECHA_CREACION="2026-06-01 12:05:00",
        FECHA_ENTREGA="2026-06-01 12:38:00",
        ESTADO=3,
        DISTANCIA_KM=3.2
    ))
    p1_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p1_id, COMBO_ID=1, CANTIDAD=2,
        PRECIO_UNITARIO=4000, CONFIGURACION="sin pepinos"
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p1_id, COMBO_ID=3, CANTIDAD=1,
        PRECIO_UNITARIO=6000, CONFIGURACION=None
    ))

    # Pedido 2 — Juan en KFC, entregado por Ana
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente_id,
        RESTAURANTE_ID=2,
        REPARTIDOR_ID=rep2_id,
        FECHA_CREACION="2026-06-02 19:10:00",
        FECHA_ENTREGA="2026-06-02 19:45:00",
        ESTADO=3,
        DISTANCIA_KM=4.8
    ))
    p2_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p2_id, COMBO_ID=10, CANTIDAD=1,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p2_id, COMBO_ID=14, CANTIDAD=1,
        PRECIO_UNITARIO=8000, CONFIGURACION="extra salsa"
    ))

    # Pedido 3 — María en Burger King, entregado por Pedro
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente2_id,
        RESTAURANTE_ID=3,
        REPARTIDOR_ID=rep_id,
        FECHA_CREACION="2026-06-03 13:20:00",
        FECHA_ENTREGA="2026-06-03 13:55:00",
        ESTADO=3,
        DISTANCIA_KM=2.1
    ))
    p3_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p3_id, COMBO_ID=19, CANTIDAD=1,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p3_id, COMBO_ID=20, CANTIDAD=1,
        PRECIO_UNITARIO=5000, CONFIGURACION="sin cebolla"
    ))

    # Pedido 4 — Juan en Pizza Hut, entregado por Ana
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente_id,
        RESTAURANTE_ID=7,
        REPARTIDOR_ID=rep2_id,
        FECHA_CREACION="2026-06-04 20:00:00",
        FECHA_ENTREGA="2026-06-04 20:42:00",
        ESTADO=3,
        DISTANCIA_KM=5.6
    ))
    p4_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p4_id, COMBO_ID=55, CANTIDAD=1,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p4_id, COMBO_ID=57, CANTIDAD=1,
        PRECIO_UNITARIO=6000, CONFIGURACION="borde relleno"
    ))

    # Pedido 5 — María en Taco Bell, entregado por Pedro
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente2_id,
        RESTAURANTE_ID=4,
        REPARTIDOR_ID=rep_id,
        FECHA_CREACION="2026-06-05 14:15:00",
        FECHA_ENTREGA="2026-06-05 14:48:00",
        ESTADO=3,
        DISTANCIA_KM=3.9
    ))
    p5_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p5_id, COMBO_ID=28, CANTIDAD=3,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p5_id, COMBO_ID=32, CANTIDAD=1,
        PRECIO_UNITARIO=7000, CONFIGURACION="sin jalapeño"
    ))

    # Pedido 6 — Juan en Domino's, entregado por Ana
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente_id,
        RESTAURANTE_ID=8,
        REPARTIDOR_ID=rep2_id,
        FECHA_CREACION="2026-06-06 21:30:00",
        FECHA_ENTREGA="2026-06-06 22:10:00",
        ESTADO=3,
        DISTANCIA_KM=6.3
    ))
    p6_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p6_id, COMBO_ID=64, CANTIDAD=1,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p6_id, COMBO_ID=67, CANTIDAD=1,
        PRECIO_UNITARIO=7000, CONFIGURACION="doble queso"
    ))

    # Pedido 7 — María en Subway, cancelado
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente2_id,
        RESTAURANTE_ID=5,
        REPARTIDOR_ID=None,
        FECHA_CREACION="2026-06-07 11:00:00",
        FECHA_ENTREGA=None,
        ESTADO=4,
        DISTANCIA_KM=0
    ))
    p7_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p7_id, COMBO_ID=37, CANTIDAD=1,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))

    # Pedido 8 — Juan en Johnny Rockets, entregado por Pedro
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente_id,
        RESTAURANTE_ID=9,
        REPARTIDOR_ID=rep_id,
        FECHA_CREACION="2026-06-08 13:00:00",
        FECHA_ENTREGA="2026-06-08 13:35:00",
        ESTADO=3,
        DISTANCIA_KM=4.1
    ))
    p8_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p8_id, COMBO_ID=73, CANTIDAD=2,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p8_id, COMBO_ID=77, CANTIDAD=1,
        PRECIO_UNITARIO=8000, CONFIGURACION="papas grandes"
    ))

    # Pedido 9 — Juan en Popeyes, en camino (activo)
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente_id,
        RESTAURANTE_ID=6,
        REPARTIDOR_ID=rep2_id,
        FECHA_CREACION="2026-06-09 18:45:00",
        FECHA_ENTREGA=None,
        ESTADO=2,
        DISTANCIA_KM=7.2
    ))
    p9_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p9_id, COMBO_ID=46, CANTIDAD=1,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p9_id, COMBO_ID=50, CANTIDAD=1,
        PRECIO_UNITARIO=8000, CONFIGURACION="extra spicy"
    ))

    # Pedido 10 — María en Quiznos, pendiente (recién creado)
    res = conn.execute(insert(pedido).values(
        CLIENTE_ID=cliente2_id,
        RESTAURANTE_ID=10,
        REPARTIDOR_ID=None,
        FECHA_CREACION="2026-06-10 12:30:00",
        FECHA_ENTREGA=None,
        ESTADO=0,
        DISTANCIA_KM=0
    ))
    p10_id = res.inserted_primary_key[0]
    conn.execute(insert(detalle_pedido).values(
        PEDIDO_ID=p10_id, COMBO_ID=82, CANTIDAD=2,
        PRECIO_UNITARIO=4000, CONFIGURACION=None
    ))

print("BD inicializada correctamente.")
print("   CLIENTE 1:    cliente@test.com    / 123456  (Juan, 6 pedidos)")
print("   CLIENTE 2:    maria@test.com      / 123456  (María, 4 pedidos)")
print("   REPARTIDOR 1: repartidor@test.com / 123456  (Pedro)")
print("   REPARTIDOR 2: ana@test.com        / 123456  (Ana)")
print("   ADMIN:        admin@cletaeats.com / admin123")