# data/repositories/cliente_repository.py
from typing import Optional
from sqlalchemy import select, insert, update, delete
from data.database.db_connection import engine
from data.database.tables import cliente as t_cliente, usuario as t_usuario, pedido as t_pedido, tarjeta_cliente as t_tarjeta
from core.entities.cliente import Cliente
from data.utils.mapper_utils import to_lower_dict

class ClienteRepository:

    def crear(self, c: Cliente) -> Cliente:
        with engine.begin() as conn:
            result = conn.execute(insert(t_cliente).values(
                USUARIO_ID=c.usuario_id,
                CEDULA=c.cedula,
                NOMBRE=c.nombre,
                DIRECCION=c.direccion,
                TELEFONO=c.telefono
            ))
            c.id = result.inserted_primary_key[0]
            return c

    def encontrar_por_usuario_id(self, usuario_id: int) -> Optional[Cliente]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_cliente).where(t_cliente.c.USUARIO_ID == usuario_id)
            ).mappings().first()
            return self._map(row) if row else None

    def existe_cedula(self, cedula: str) -> bool:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_cliente.c.ID).where(t_cliente.c.CEDULA == cedula)
            ).first()
            return row is not None

    def listar_todos(self) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_cliente.c.ID,
                    t_cliente.c.NOMBRE,
                    t_cliente.c.CEDULA,
                    t_cliente.c.TELEFONO,
                    t_cliente.c.DIRECCION,
                    t_usuario.c.EMAIL,
                    t_usuario.c.ESTADO,
                    t_usuario.c.ID.label("usuario_id")
                ).join(t_usuario, t_cliente.c.USUARIO_ID == t_usuario.c.ID)
                .order_by(t_cliente.c.ID)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def actualizar_estado_usuario(self, cliente_id: int, estado: int) -> bool:
        """Activa/desactiva el usuario asociado al cliente."""
        with engine.begin() as conn:
            # Obtener usuario_id del cliente
            row = conn.execute(
                select(t_cliente.c.USUARIO_ID).where(t_cliente.c.ID == cliente_id)
            ).first()
            if not row:
                return False
            result = conn.execute(
                update(t_usuario)
                .where(t_usuario.c.ID == row[0])
                .values(ESTADO=estado)
            )
            return result.rowcount > 0

    def actualizar_imagen_url(self, cliente_id: int, url: str) -> bool:
        with engine.begin() as conn:
            result = conn.execute(
                update(t_cliente).where(t_cliente.c.ID == cliente_id).values(IMAGEN_URL=url)
            )
            return result.rowcount > 0

    def eliminar(self, cliente_id: int) -> bool:
        with engine.connect() as conn:
            if conn.execute(select(t_pedido.c.ID).where(t_pedido.c.CLIENTE_ID == cliente_id).limit(1)).first():
                raise ValueError("No se puede eliminar el cliente porque tiene pedidos asociados")
            if conn.execute(select(t_tarjeta.c.ID).where(t_tarjeta.c.CLIENTE_ID == cliente_id).limit(1)).first():
                raise ValueError("No se puede eliminar el cliente porque tiene tarjetas asociadas")

        with engine.begin() as conn:
            result = conn.execute(
                delete(t_cliente).where(t_cliente.c.ID == cliente_id)
            )
            return result.rowcount > 0

    @staticmethod
    def _map(row) -> Cliente:
        return Cliente(
            id=row["ID"],
            usuario_id=row["USUARIO_ID"],
            cedula=row["CEDULA"],
            nombre=row["NOMBRE"],
            direccion=row["DIRECCION"],
            telefono=row["TELEFONO"],
            imagen_url=row["IMAGEN_URL"] or ""
        )