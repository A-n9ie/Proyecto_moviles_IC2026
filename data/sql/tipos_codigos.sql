-- =========================
-- TIPO
-- =========================

INSERT INTO TIPO (NOMBRE) VALUES
('ESTADO_REGISTRO'),
('TIPO_HABITO'),
('DIA_SEMANA');

-- =========================
-- ESTADO REGISTRO
-- =========================

INSERT INTO CODIGO (ID_TIPO, VALOR, DESPLIEGUE) VALUES
(1, 'COMPLETO', 'Completo'),
(1, 'PENDIENTE', 'Pendiente'),
(1, 'INCOMPLETO', 'Incompleto');

-- =========================
-- TIPO HABITO
-- =========================

INSERT INTO CODIGO (ID_TIPO, VALOR, DESPLIEGUE) VALUES
(2, 'FIJO', 'Fijo'),
(2, 'INTERVALO', 'Intervalo');

-- =========================
-- DIAS DE SEMANA
-- =========================

INSERT INTO CODIGO (ID_TIPO, VALOR, DESPLIEGUE) VALUES
(3, 'LUNES', 'Lunes'),
(3, 'MARTES', 'Martes'),
(3, 'MIERCOLES', 'Miércoles'),
(3, 'JUEVES', 'Jueves'),
(3, 'VIERNES', 'Viernes'),
(3, 'SABADO', 'Sábado'),
(3, 'DOMINGO', 'Domingo');

-- DATOS DE PRUEBA
INSERT INTO USUARIO
(NOMBRE_USUARIO, EMAIL, PASSWORD, ESTADO_USUARIO)
VALUES ('Max', 'max@test.com', '123', 1);

INSERT INTO HABITO
(NOMBRE_HABITO, DESCRIPCION, ID_CODIGO_TIPO_HABITO, ESTADO_HABITO, ID_USUARIO)
VALUES 
('Caminar', 'Pasear 30 min', 4, 1, 1),
('No ladrar', 'Ver perros sin ladrar', 4, 1, 1);