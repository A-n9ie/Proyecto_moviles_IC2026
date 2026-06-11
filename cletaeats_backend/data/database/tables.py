# data/database/tables.py
from sqlalchemy import (
    Table, Column, Integer, Text, Float, ForeignKey, CheckConstraint,
    UniqueConstraint, PrimaryKeyConstraint, func
)
from data.database.db_connection import metadata

usuario = Table("USUARIO", metadata,
    Column("ID",             Integer, primary_key=True, autoincrement=True),
    Column("EMAIL",          Text,    nullable=False, unique=True),
    Column("PASSWORD_HASH",  Text,    nullable=False),
    Column("ROL",            Text,    nullable=False),
    Column("ESTADO",         Integer, nullable=False, default=1),
    Column("FECHA_REGISTRO", Text,    nullable=False, server_default=func.datetime("now")),
    CheckConstraint("ROL IN ('CLIENTE','REPARTIDOR','ADMIN','RESTAURANTE')", name="ck_usuario_rol"),
    CheckConstraint("ESTADO IN (0,1)", name="ck_usuario_estado"),
)

cliente = Table("CLIENTE", metadata,
    Column("ID",         Integer, primary_key=True, autoincrement=True),
    Column("USUARIO_ID", Integer, ForeignKey("USUARIO.ID", ondelete="CASCADE", onupdate="CASCADE"), nullable=False, unique=True),
    Column("IMAGEN_URL", Text,    default=""),
    Column("CEDULA",     Text,    nullable=False, unique=True),
    Column("NOMBRE",     Text,    nullable=False),
    Column("DIRECCION",  Text,    nullable=False),
    Column("TELEFONO",   Text,    nullable=False, unique=True),
)

repartidor = Table("REPARTIDOR", metadata,
    Column("ID",                    Integer, primary_key=True, autoincrement=True),
    Column("USUARIO_ID",            Integer, ForeignKey("USUARIO.ID", ondelete="CASCADE", onupdate="CASCADE"), nullable=False, unique=True),
    Column("IMAGEN_URL",            Text,    default=""),
    Column("CEDULA",                Text,    nullable=False, unique=True),
    Column("NOMBRE",                Text,    nullable=False),
    Column("CORREO",                Text,    nullable=False, unique=True),
    Column("DIRECCION",             Text,    nullable=False),
    Column("TELEFONO",              Text,    nullable=False, unique=True),
    Column("TARJETA",               Text,    nullable=False),
    Column("DISPONIBLE", Integer, nullable=False, default=1),
    Column("KM_RECORRIDOS_DIARIOS", Float,   nullable=False, default=0),
    Column("COSTO_KM_HABIL",        Float,   nullable=False, default=1000),
    Column("COSTO_KM_FERIADO",      Float,   nullable=False, default=1500),
    Column("AMONESTACIONES",        Integer, nullable=False, default=0),
    Column("RATING", Float, nullable=False, default=0.0),
    CheckConstraint("DISPONIBLE IN (0,1)",                     name="ck_repartidor_disponible"),
    CheckConstraint("AMONESTACIONES >= 0 AND AMONESTACIONES <= 4", name="ck_repartidor_amones"),
    
)

restaurante = Table("RESTAURANTE", metadata,
    Column("ID",              Integer, primary_key=True, autoincrement=True),
    Column("CEDULA_JURIDICA", Text,    nullable=False, unique=True),
    Column("NOMBRE",          Text,    nullable=False),
    Column("DIRECCION",       Text,    nullable=False),
    Column("ESTADO",          Integer, nullable=False, default=1),
    Column("IMAGEN_URL",      Text),
    Column("LATITUD",         Float),
    Column("LONGITUD",        Float),
    CheckConstraint("ESTADO IN (0,1)", name="ck_restaurante_estado"),
)

categoria = Table("CATEGORIA", metadata,
    Column("ID",     Integer, primary_key=True, autoincrement=True),
    Column("NOMBRE", Text,    nullable=False, unique=True),
)

restaurante_categoria = Table("RESTAURANTE_CATEGORIA", metadata,
    Column("RESTAURANTE_ID", Integer, ForeignKey("RESTAURANTE.ID", ondelete="CASCADE"), nullable=False),
    Column("CATEGORIA_ID",   Integer, ForeignKey("CATEGORIA.ID",   ondelete="CASCADE"), nullable=False),
    PrimaryKeyConstraint("RESTAURANTE_ID", "CATEGORIA_ID"),
)

producto = Table("PRODUCTO", metadata,
    Column("ID",             Integer, primary_key=True, autoincrement=True),
    Column("RESTAURANTE_ID", Integer, ForeignKey("RESTAURANTE.ID", ondelete="CASCADE"), nullable=False),
    Column("NOMBRE",         Text,    nullable=False),
    Column("DESCRIPCION",    Text),
    Column("ESTADO",         Integer, nullable=False, default=1),
    CheckConstraint("ESTADO IN (0,1)", name="ck_producto_estado"),
)

combo = Table("COMBO", metadata,
    Column("ID",             Integer, primary_key=True, autoincrement=True),
    Column("RESTAURANTE_ID", Integer, ForeignKey("RESTAURANTE.ID", ondelete="CASCADE", onupdate="CASCADE"), nullable=False),
    Column("NUMERO_COMBO",   Integer, nullable=False),
    Column("NOMBRE",         Text,    nullable=False),
    Column("DESCRIPCION",    Text),
    Column("PRECIO",         Float,   nullable=False),
    Column("IMAGEN_URL",     Text),
    Column("ESTADO",         Integer, nullable=False, default=1),
    CheckConstraint("NUMERO_COMBO BETWEEN 1 AND 9", name="ck_combo_numero"),
    CheckConstraint("ESTADO IN (0,1)",               name="ck_combo_estado"),
)

combo_producto = Table("COMBO_PRODUCTO", metadata,
    Column("COMBO_ID",    Integer, ForeignKey("COMBO.ID",    ondelete="CASCADE"),   nullable=False),
    Column("PRODUCTO_ID", Integer, ForeignKey("PRODUCTO.ID", ondelete="RESTRICT"),  nullable=False),
    PrimaryKeyConstraint("COMBO_ID", "PRODUCTO_ID"),
)

tarjeta_cliente = Table("TARJETA_CLIENTE", metadata,
    Column("ID",          Integer, primary_key=True, autoincrement=True),
    Column("CLIENTE_ID",  Integer, ForeignKey("CLIENTE.ID", ondelete="CASCADE"), nullable=False),
    Column("NUMERO",             Text,    nullable=False),
    Column("ALIAS",              Text),
    Column("FECHA_VENCIMIENTO",  Text),       # MM/YY
    Column("CVV",                Text),       # 3-4 dígitos (solo en tránsito, no persistir en prod)
    Column("ES_PRINCIPAL",       Integer, nullable=False, default=0),
    CheckConstraint("ES_PRINCIPAL IN (0,1)", name="ck_tarjeta_principal"),
)

pedido = Table("PEDIDO", metadata,
    Column("ID",             Integer, primary_key=True, autoincrement=True),
    Column("CLIENTE_ID",     Integer, ForeignKey("CLIENTE.ID",     ondelete="RESTRICT", onupdate="CASCADE"), nullable=False),
    Column("RESTAURANTE_ID", Integer, ForeignKey("RESTAURANTE.ID", ondelete="RESTRICT", onupdate="CASCADE"), nullable=False),
    Column("REPARTIDOR_ID",  Integer, ForeignKey("REPARTIDOR.ID",  ondelete="SET NULL",  onupdate="CASCADE")),
    Column("FECHA_CREACION", Text,    nullable=False, server_default=func.datetime("now")),
    Column("FECHA_ENTREGA",  Text),
    Column("ESTADO",         Integer, nullable=False, default=0),
    Column("DISTANCIA_KM",   Float,   nullable=False, default=0),
    CheckConstraint("ESTADO IN (0,1,2,3,4)", name="ck_pedido_estado"),
)

detalle_pedido = Table("DETALLE_PEDIDO", metadata,
    Column("ID",              Integer, primary_key=True, autoincrement=True),
    Column("PEDIDO_ID",       Integer, ForeignKey("PEDIDO.ID",  ondelete="CASCADE"),  nullable=False),
    Column("COMBO_ID",        Integer, ForeignKey("COMBO.ID",   ondelete="RESTRICT"), nullable=False),
    Column("CANTIDAD",        Integer, nullable=False, default=1),
    Column("PRECIO_UNITARIO", Float,   nullable=False),
    Column("CONFIGURACION",   Text),
    CheckConstraint("CANTIDAD > 0", name="ck_detalle_cantidad"),
)