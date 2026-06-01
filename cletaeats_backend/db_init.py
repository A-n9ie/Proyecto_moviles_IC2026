# db_init.py
import os
from sqlalchemy import insert, text
from data.database.db_connection import engine, DB_PATH
from data.database.tables import (
    metadata,
    restaurante, categoria, restaurante_categoria,
    combo, usuario, cliente, tarjeta_cliente, repartidor
)
from services.hash_service import hash_password

print(f"Inicializando BD en: {DB_PATH}")
os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
#Borrar BD existente para recrearla con datos frescos
if os.path.exists(DB_PATH):
    os.remove(DB_PATH)
    print("BD anterior eliminada.")

metadata.create_all(engine)

# ── Datos seed ──────────────────────────────────────────────────────
# (cedula, nombre, direccion, imagen_url, latitud, longitud)
RESTAURANTES_SEED = [
    # (cedula,          nombre,          direccion,                    imagen_url,           latitud,   longitud)
    ("3-101-000001", "Burger House",   "San José, Avenida Central",   "placeholder_burger",  9.9281,  -84.0907),  # San José
    ("3-101-000002", "Dragon Palace",  "Alajuela, Parque Central",    "placeholder_dragon",  10.0162, -84.2141),  # Alajuela
    ("3-101-000003", "Green Garden",   "Cartago, Las Ruinas",         "placeholder_garden",  9.8647,  -83.9193),  # Cartago
    ("3-101-000004", "Pizza Roma",     "Heredia, Parque Central",     "placeholder_pizza",   9.9981,  -84.1170),  # Heredia
    ("3-101-000005", "Taco Fiesta",    "Guanacaste, Liberia Centro",  "placeholder_taco",    10.6340, -85.4374),  # Guanacaste
    ("3-101-000006", "Mar y Tierra",   "Puntarenas, Malecón",         "placeholder_mar",     9.9760,  -84.8282),  # Puntarenas
    ("3-101-000007", "Sushi Zen",      "Limón, Parque Vargas",        "placeholder_sushi",   9.9907,  -83.0361),  # Limón
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

        conn.execute(insert(restaurante_categoria).values(
            RESTAURANTE_ID=rest_id,
            CATEGORIA_ID=cat_ids[cat_por_restaurante[nombre]]
        ))

        for i, nombre_combo in enumerate(COMBOS_POR_RESTAURANTE[nombre], start=1):
            precio = 3000 + (i * 1000)
            conn.execute(insert(combo).values(
                RESTAURANTE_ID=rest_id,
                NUMERO_COMBO=i,
                NOMBRE=nombre_combo,
                PRECIO=precio,
                IMAGEN_URL="placeholder_combo"
            ))

    # Usuario cliente de prueba
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
        ES_PRINCIPAL=1
    ))

    # Usuario repartidor de prueba
    res = conn.execute(insert(usuario).values(
        EMAIL="repartidor@test.com",
        PASSWORD_HASH=hash_password("123456"),
        ROL="REPARTIDOR",
        ESTADO=1
    ))
    u_rep_id = res.inserted_primary_key[0]
    conn.execute(insert(repartidor).values(
        USUARIO_ID=u_rep_id,
        CEDULA="1-0002-0002",
        NOMBRE="Pedro Repartidor",
        CORREO="repartidor@test.com",
        DIRECCION="Heredia, San Francisco",
        TELEFONO="8888-0002",
        TARJETA="4111111111111112",
        ESTADO=1,
        KM_RECORRIDOS_DIARIOS=0,
        AMONESTACIONES=0
    ))

    # Usuario admin
    conn.execute(insert(usuario).values(
        EMAIL="admin@cletaeats.com",
        PASSWORD_HASH=hash_password("admin123"),
        ROL="ADMIN",
        ESTADO=1
    ))

print("BD inicializada.")
print("   CLIENTE:    cliente@test.com    / 123456")
print("   REPARTIDOR: repartidor@test.com / 123456")
print("   ADMIN:      admin@cletaeats.com / admin123")