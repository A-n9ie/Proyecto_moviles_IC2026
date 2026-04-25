-- =========================
-- USUARIO
-- =========================

INSERT INTO USUARIO (EMAIL, PASSWORD_HASH, ROL, ESTADO)
VALUES 
('cliente1@mail.com', 'hash123', 'CLIENTE', 1),
('cliente2@mail.com', 'hash123', 'CLIENTE', 1),

('repartidor1@mail.com', 'hash123', 'REPARTIDOR', 1),
('repartidor2@mail.com', 'hash123', 'REPARTIDOR', 1),

('admin@mail.com', 'hash123', 'ADMIN', 1),

('restaurante1@mail.com', 'hash123', 'RESTAURANTE', 1);

-- =========================
-- CLIENTE
-- =========================

INSERT INTO CLIENTE (USUARIO_ID, CEDULA, NOMBRE, DIRECCION, TELEFONO, TARJETA)
VALUES 
(1, '1-1111-1111', 'Ana Pérez', 'Heredia Centro', '8888-1111', '1111222233334444'),
(2, '2-2222-2222', 'Luis Gómez', 'San Pablo', '8888-2222', '5555666677778888');

-- =========================
-- REPARTIDOR
-- =========================

INSERT INTO REPARTIDOR (
    USUARIO_ID, CEDULA, NOMBRE, CORREO, DIRECCION, TELEFONO, TARJETA,
    ESTADO, KM_RECORRIDOS_DIARIOS, AMONESTACIONES
)
VALUES 
(3, '3-3333-3333', 'Carlos Ruiz', 'carlo@mail.com', 'Heredia', '8888-3333', '1234123412341234', 1, 10, 0),
(4, '4-4444-4444', 'María Solís', 'maria@mail.com', 'Santo Domingo', '8888-4444', '9876987698769876', 1, 5, 1);

-- =========================
-- RESTAURANTE
-- =========================

INSERT INTO RESTAURANTE (CEDULA_JURIDICA, NOMBRE, DIRECCION, TIPO_COMIDA)
VALUES 
('3-101-111111', 'Pizza Express', 'Heredia Centro', 'RÁPIDA'),
('3-101-222222', 'Sushi House', 'San Pablo', 'JAPONESA');

-- =========================
-- COMBO
-- =========================

INSERT INTO COMBO (RESTAURANTE_ID, NUMERO_COMBO, NOMBRE, DESCRIPCION, PRECIO, ESTADO)
VALUES 
(1, 1, 'Pizza Personal', 'Pizza básica', 4000, 1),
(1, 2, 'Pizza Mediana', 'Pizza mediana', 5000, 1),
(1, 3, 'Pizza Grande', 'Pizza grande', 6000, 1),

(2, 1, 'Sushi Básico', 'Sushi mixto', 4500, 1),
(2, 2, 'Sushi Deluxe', 'Sushi especial', 7000, 1);

-- =========================
-- PEDIDO
-- =========================

INSERT INTO PEDIDO (
    CLIENTE_ID, RESTAURANTE_ID, REPARTIDOR_ID,
    ESTADO, DISTANCIA_KM
)
VALUES 
(1, 1, 1, 1, 3.5);

-- =========================
-- DETALLE_PEDIDO
-- =========================

INSERT INTO DETALLE_PEDIDO (
    PEDIDO_ID, COMBO_ID, CANTIDAD, PRECIO_UNITARIO, CONFIGURACION
)
VALUES 
(1, 1, 2, 4000, '{"SIN_CEBOLLA": true, "BEBIDA": "GRANDE"}'),
(1, 2, 1, 5000, '{"EXTRA_QUESO": true}');