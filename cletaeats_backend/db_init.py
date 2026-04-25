# db_init.py
"""
Ejecutar UNA SOLA VEZ (o cuando se quiera reiniciar la BD):
    python db_init.py
"""
from data.database.db_connection import get_connection, DB_PATH
from services.hash_service import hash_password
import os

print(f"Inicializando BD en: {DB_PATH}")

SQL_SCHEMA = """
PRAGMA FOREIGN_KEYS = OFF;

DROP TABLE IF EXISTS DETALLE_PEDIDO;
DROP TABLE IF EXISTS PEDIDO;
DROP TABLE IF EXISTS COMBO;
DROP TABLE IF EXISTS RESTAURANTE;
DROP TABLE IF EXISTS REPARTIDOR;
DROP TABLE IF EXISTS CLIENTE;
DROP TABLE IF EXISTS USUARIO;

PRAGMA FOREIGN_KEYS = ON;

CREATE TABLE USUARIO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    EMAIL TEXT NOT NULL UNIQUE,
    PASSWORD_HASH TEXT NOT NULL,
    ROL TEXT NOT NULL CHECK (ROL IN ('CLIENTE','REPARTIDOR','ADMIN','RESTAURANTE')),
    ESTADO INTEGER NOT NULL DEFAULT 1 CHECK (ESTADO IN (0,1)),
    FECHA_REGISTRO TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE CLIENTE (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    USUARIO_ID INTEGER NOT NULL UNIQUE,
    CEDULA TEXT NOT NULL UNIQUE,
    NOMBRE TEXT NOT NULL,
    DIRECCION TEXT NOT NULL,
    TELEFONO TEXT NOT NULL UNIQUE,
    TARJETA TEXT NOT NULL,
    FOREIGN KEY (USUARIO_ID) REFERENCES USUARIO(ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE REPARTIDOR (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    USUARIO_ID INTEGER NOT NULL UNIQUE,
    CEDULA TEXT NOT NULL UNIQUE,
    NOMBRE TEXT NOT NULL,
    CORREO TEXT NOT NULL UNIQUE,
    DIRECCION TEXT NOT NULL,
    TELEFONO TEXT NOT NULL UNIQUE,
    TARJETA TEXT NOT NULL,
    ESTADO INTEGER NOT NULL DEFAULT 1 CHECK (ESTADO IN (0,1)),
    KM_RECORRIDOS_DIARIOS REAL NOT NULL DEFAULT 0,
    COSTO_KM_HABIL REAL NOT NULL DEFAULT 1000,
    COSTO_KM_FERIADO REAL NOT NULL DEFAULT 1500,
    AMONESTACIONES INTEGER NOT NULL DEFAULT 0 CHECK (AMONESTACIONES >= 0 AND AMONESTACIONES <= 4),
    FOREIGN KEY (USUARIO_ID) REFERENCES USUARIO(ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE RESTAURANTE (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    CEDULA_JURIDICA TEXT NOT NULL UNIQUE,
    NOMBRE TEXT NOT NULL,
    DIRECCION TEXT NOT NULL,
    TIPO_COMIDA TEXT NOT NULL,
    ESTADO INTEGER NOT NULL DEFAULT 1 CHECK (ESTADO IN (0,1)),
    IMAGEN_URL TEXT
);

CREATE TABLE COMBO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    RESTAURANTE_ID INTEGER NOT NULL,
    NUMERO_COMBO INTEGER NOT NULL,
    NOMBRE TEXT NOT NULL,
    DESCRIPCION TEXT,
    PRECIO REAL NOT NULL,
    IMAGEN_URL TEXT,
    ESTADO INTEGER NOT NULL DEFAULT 1 CHECK (ESTADO IN (0,1)),
    FOREIGN KEY (RESTAURANTE_ID) REFERENCES RESTAURANTE(ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (NUMERO_COMBO BETWEEN 1 AND 9)
);

CREATE TABLE PEDIDO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    CLIENTE_ID INTEGER NOT NULL,
    RESTAURANTE_ID INTEGER NOT NULL,
    REPARTIDOR_ID INTEGER,
    FECHA_CREACION TEXT NOT NULL DEFAULT (datetime('now')),
    FECHA_ENTREGA TEXT,
    ESTADO INTEGER NOT NULL DEFAULT 0 CHECK (ESTADO IN (0,1,2,3,4)),
    DISTANCIA_KM REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (CLIENTE_ID) REFERENCES CLIENTE(ID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (RESTAURANTE_ID) REFERENCES RESTAURANTE(ID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (REPARTIDOR_ID) REFERENCES REPARTIDOR(ID) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE DETALLE_PEDIDO (
    ID INTEGER PRIMARY KEY AUTOINCREMENT,
    PEDIDO_ID INTEGER NOT NULL,
    COMBO_ID INTEGER NOT NULL,
    CANTIDAD INTEGER NOT NULL DEFAULT 1 CHECK (CANTIDAD > 0),
    PRECIO_UNITARIO REAL NOT NULL,
    CONFIGURACION TEXT,
    FOREIGN KEY (PEDIDO_ID) REFERENCES PEDIDO(ID) ON DELETE CASCADE,
    FOREIGN KEY (COMBO_ID) REFERENCES COMBO(ID) ON DELETE RESTRICT
);
"""

RESTAURANTES_SEED = [
    ("3-101-000001", "Burger House",    "Heredia Centro",        "rápida",    "placeholder_burger"),
    ("3-101-000002", "Dragon Palace",   "Heredia, San Francisco", "china",     "placeholder_dragon"),
    ("3-101-000003", "Green Garden",    "Heredia, Barreal",       "saludable", "placeholder_garden"),
    ("3-101-000004", "Pizza Roma",      "Heredia, Mercedes",      "italiana",  "placeholder_pizza"),
    ("3-101-000005", "Taco Fiesta",     "Heredia, Ulloa",         "mexicana",  "placeholder_taco"),
    ("3-101-000006", "Mar y Tierra",    "Heredia, La Aurora",     "mariscos",  "placeholder_mar"),
    ("3-101-000007", "Sushi Zen",       "Heredia, Belén",         "japonesa",  "placeholder_sushi"),
]

# Nombres de combos por restaurante (9 por cada uno)
COMBOS_POR_RESTAURANTE = {
    "Burger House":  ["Clásica Simple","Doble Cheese","BBQ Crispy","Mushroom Swiss","Bacon Deluxe","Mega Combo","Tower Burger","Signature","Ultimate Beast"],
    "Dragon Palace": ["Arroz Frito Básico","Chop Suey","Lo Mein Especial","Dim Sum Mix","Pato Pekín","Cerdo Agridulce","Mariscos Wok","Tofu Especial","Banquete Imperial"],
    "Green Garden":  ["Bowl Verde","Ensalada César","Wrap Vegano","Smoothie Bowl","Quinoa Mix","Buddha Bowl","Power Salad","Detox Plate","Super Green"],
    "Pizza Roma":    ["Margarita","Pepperoni","Quattro Stagioni","Hawaiana","Carbonara","Diavola","Prosciutto","Funghi","Gran Roma"],
    "Taco Fiesta":   ["Taco Básico","Burrito Clásico","Quesadilla","Nachos","Enchiladas","Fajitas","Chimichanga","Tostadas","Combo Fiesta"],
    "Mar y Tierra":  ["Ceviche","Camarones al Ajillo","Filete Tilapia","Corvina Frita","Pulpo","Mixto Mariscos","Langostinos","Almejas","Banquete del Mar"],
    "Sushi Zen":     ["Maki Clásico","Nigiri Set","Sashimi","Temaki","Uramaki","Spicy Roll","Dragon Roll","Rainbow Roll","Chef's Special"],
}


def seed():
    conn = get_connection()

    # Crear schema
    conn.executescript(SQL_SCHEMA)

    # ── Restaurantes + Combos ─────────────────────────────────────
    for ced, nombre, dir_, tipo, img in RESTAURANTES_SEED:
        cursor = conn.execute(
            "INSERT INTO RESTAURANTE (CEDULA_JURIDICA, NOMBRE, DIRECCION, TIPO_COMIDA, IMAGEN_URL) VALUES (?,?,?,?,?)",
            (ced, nombre, dir_, tipo, img)
        )
        restaurante_id = cursor.lastrowid
        combos = COMBOS_POR_RESTAURANTE[nombre]
        for i, nombre_combo in enumerate(combos, start=1):
            precio = 3000 + (i * 1000)  # Combo 1=¢4000 ... Combo 9=¢12000
            conn.execute(
                "INSERT INTO COMBO (RESTAURANTE_ID, NUMERO_COMBO, NOMBRE, PRECIO, IMAGEN_URL) VALUES (?,?,?,?,?)",
                (restaurante_id, i, nombre_combo, precio, "placeholder_combo")
            )

    # ── Usuario de prueba: CLIENTE ────────────────────────────────
    cursor = conn.execute(
        "INSERT INTO USUARIO (EMAIL, PASSWORD_HASH, ROL) VALUES (?,?,?)",
        ("cliente@test.com", hash_password("123456"), "CLIENTE")
    )
    u_id = cursor.lastrowid
    conn.execute(
        "INSERT INTO CLIENTE (USUARIO_ID, CEDULA, NOMBRE, DIRECCION, TELEFONO, TARJETA) VALUES (?,?,?,?,?,?)",
        (u_id, "1-1111-1111", "Juan Cliente", "Heredia Centro", "88881111", "4000111122223333")
    )

    # ── Usuario de prueba: REPARTIDOR ─────────────────────────────
    cursor = conn.execute(
        "INSERT INTO USUARIO (EMAIL, PASSWORD_HASH, ROL) VALUES (?,?,?)",
        ("repartidor@test.com", hash_password("123456"), "REPARTIDOR")
    )
    u_id = cursor.lastrowid
    conn.execute(
        """INSERT INTO REPARTIDOR (USUARIO_ID, CEDULA, NOMBRE, CORREO, DIRECCION, TELEFONO, TARJETA)
           VALUES (?,?,?,?,?,?,?)""",
        (u_id, "2-2222-2222", "Pedro Repartidor", "repartidor@test.com",
         "Heredia, La Aurora", "88882222", "4000222233334444")
    )

    conn.commit()
    conn.close()
    print("✅ BD inicializada con 7 restaurantes, 63 combos y 2 usuarios de prueba.")
    print("   CLIENTE:     cliente@test.com    / 123456")
    print("   REPARTIDOR:  repartidor@test.com / 123456")


if __name__ == "__main__":
    seed()