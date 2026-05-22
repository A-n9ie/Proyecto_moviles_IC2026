# db_init.py
"""
Ejecutar UNA SOLA VEZ (o cuando se quiera reiniciar la BD):
    python db_init.py
"""
import os
from sqlalchemy import insert, text
from data.database.db_connection import engine, DB_PATH
from data.database.tables import (
    metadata, usuario, cliente, repartidor,
    restaurante, categoria, restaurante_categoria,
    producto, combo, pedido
)
from services.hash_service import hash_password

# Crear directorio si no existe
os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
print(f"Inicializando BD en: {DB_PATH}")

# ── Crear todas las tablas ──────────────────────────────────────────
with engine.begin() as conn:
    conn.execute(text("PRAGMA foreign_keys = OFF"))
    metadata.drop_all(engine)
    metadata.create_all(engine)
    conn.execute(text("PRAGMA foreign_keys = ON"))

# ── Datos seed ──────────────────────────────────────────────────────
RESTAURANTES_SEED = [
    ("3-101-000001", "Burger House",  "Heredia Centro",         "placeholder_burger"),
    ("3-101-000002", "Dragon Palace", "Heredia, San Francisco",  "placeholder_dragon"),
    ("3-101-000003", "Green Garden",  "Heredia, Barreal",        "placeholder_garden"),
    ("3-101-000004", "Pizza Roma",    "Heredia, Mercedes",       "placeholder_pizza"),
    ("3-101-000005", "Taco Fiesta",   "Heredia, Ulloa",          "placeholder_taco"),
    ("3-101-000006", "Mar y Tierra",  "Heredia, La Aurora",      "placeholder_mar"),
    ("3-101-000007", "Sushi Zen",     "Heredia, Belén",          "placeholder_sushi"),
]

CATEGORIAS_SEED = ["rápida", "china", "saludable", "italiana", "mexicana", "mariscos", "japonesa"]

COMBOS_POR_RESTAURANTE = {
    "Burger House":  ["Clásica Simple","Doble Cheese","BBQ Crispy","Mushroom Swiss","Bacon Deluxe","Mega Combo","Tower Burger","Signature","Ultimate Beast"],
    "Dragon Palace": ["Arroz Frito Básico","Chop Suey","Lo Mein Especial","Dim Sum Mix","Pato Pekín","Cerdo Agridulce","Mariscos Wok","Tofu Especial","Banquete Imperial"],
    "Green Garden":  ["Bowl Verde","Ensalada César","Wrap Vegano","Smoothie Bowl","Quinoa Mix","Buddha Bowl","Power Salad","Detox Plate","Super Green"],
    "Pizza Roma":    ["Margarita","Pepperoni","Quattro Stagioni","Hawaiana","Carbonara","Diavola","Prosciutto","Funghi","Gran Roma"],
    "Taco Fiesta":   ["Taco Básico","Burrito Clásico","Quesadilla","Nachos","Enchiladas","Fajitas","Chimichanga","Tostadas","Combo Fiesta"],
    "Mar y Tierra":  ["Ceviche","Camarones al Ajillo","Filete Tilapia","Corvina Frita","Pulpo","Mixto Mariscos","Langostinos","Almejas","Banquete del Mar"],
    "Sushi Zen":     ["Maki Clásico","Nigiri Set","Sashimi","Temaki","Uramaki","Spicy Roll","Dragon Roll","Rainbow Roll","Chef's Special"],
}

with engine.begin() as conn:
    conn.execute(text("PRAGMA foreign_keys = ON"))

    # Categorías
    cat_ids = {}
    for nombre_cat in CATEGORIAS_SEED:
        result = conn.execute(insert(categoria).values(NOMBRE=nombre_cat))
        cat_ids[nombre_cat] = result.inserted_primary_key[0]

    # Restaurantes + combos + relación categoría
    cat_por_restaurante = dict(zip(
        [r[1] for r in RESTAURANTES_SEED],
        CATEGORIAS_SEED
    ))
    for ced, nombre, dir_, img in RESTAURANTES_SEED:
        res = conn.execute(insert(restaurante).values(
            CEDULA_JURIDICA=ced, NOMBRE=nombre, DIRECCION=dir_, IMAGEN_URL=img
        ))
        rest_id = res.inserted_primary_key[0]

        # Asignar categoría
        conn.execute(insert(restaurante_categoria).values(
            RESTAURANTE_ID=rest_id,
            CATEGORIA_ID=cat_ids[cat_por_restaurante[nombre]]
        ))

        # Combos
        for i, nombre_combo in enumerate(COMBOS_POR_RESTAURANTE[nombre], start=1):
            precio = 3000 + (i * 1000)
            conn.execute(insert(combo).values(
                RESTAURANTE_ID=rest_id,
                NUMERO_COMBO=i,
                NOMBRE=nombre_combo,
                PRECIO=precio,
                IMAGEN_URL="placeholder_combo"
            ))

    # Usuario CLIENTE de prueba
    res = conn.execute(insert(usuario).values(
        EMAIL="cliente@test.com",
        PASSWORD_HASH=hash_password("123456"),
        ROL="CLIENTE"
    ))
    u_cliente_id = res.inserted_primary_key[0]
    res = conn.execute(insert(cliente).values(
        USUARIO_ID=u_cliente_id,
        CEDULA="1-1111-1111",
        NOMBRE="Juan Cliente",
        DIRECCION="Heredia Centro",
        TELEFONO="88881111"
    ))
    cliente_id = res.inserted_primary_key[0]
    # Tarjeta del cliente (tabla TARJETA_CLIENTE)
    from data.database.tables import tarjeta_cliente
    conn.execute(insert(tarjeta_cliente).values(
        CLIENTE_ID=cliente_id,
        NUMERO="4000111122223333",
        ALIAS="Visa principal",
        ES_PRINCIPAL=1
    ))

    # Usuario REPARTIDOR de prueba
    res = conn.execute(insert(usuario).values(
        EMAIL="repartidor@test.com",
        PASSWORD_HASH=hash_password("123456"),
        ROL="REPARTIDOR"
    ))
    u_rep_id = res.inserted_primary_key[0]
    conn.execute(insert(repartidor).values(
        USUARIO_ID=u_rep_id,
        CEDULA="2-2222-2222",
        NOMBRE="Pedro Repartidor",
        CORREO="repartidor@test.com",
        DIRECCION="Heredia, La Aurora",
        TELEFONO="88882222",
        TARJETA="4000222233334444"
    ))

    # Admin
    conn.execute(insert(usuario).values(
        EMAIL="admin@cletaeats.com",
        PASSWORD_HASH=hash_password("admin123"),
        ROL="ADMIN"
    ))

print("✅ BD inicializada.")
print("   CLIENTE:    cliente@test.com    / 123456")
print("   REPARTIDOR: repartidor@test.com / 123456")
print("   ADMIN:      admin@cletaeats.com / admin123")