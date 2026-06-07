# data/repositories/pedido_repository.py
from typing import Optional
from sqlalchemy import select, insert, update
from data.database.db_connection import engine
from data.database.tables import (
    pedido as t_pedido, detalle_pedido as t_detalle,
    cliente as t_cliente, restaurante as t_rest,
    repartidor as t_rep, combo as t_combo
)
from core.entities.pedido import Pedido
from data.utils.mapper_utils import to_lower_dict

class PedidoRepository:

    def crear_con_detalles(self, pedido: Pedido, items: list) -> Pedido:
        with engine.begin() as conn:
            result = conn.execute(insert(t_pedido).values(
                CLIENTE_ID=pedido.cliente_id,
                RESTAURANTE_ID=pedido.restaurante_id,
                REPARTIDOR_ID=pedido.repartidor_id,
                ESTADO=pedido.estado,
                DISTANCIA_KM=pedido.distancia_km
            ))
            pedido.id = result.inserted_primary_key[0]
            for item in items:
                conn.execute(insert(t_detalle).values(
                    PEDIDO_ID=pedido.id,
                    COMBO_ID=item["combo_id"],
                    CANTIDAD=item["cantidad"],
                    PRECIO_UNITARIO=item["precio_unitario"],
                    CONFIGURACION=item.get("configuracion", "{}")
                ))
        return pedido

    def obtener_por_id(self, pedido_id: int) -> Optional[Pedido]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_pedido).where(t_pedido.c.ID == pedido_id)
            ).mappings().first()
            return self._map(row) if row else None

    def listar_por_cliente(self, cliente_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.DISTANCIA_KM,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE")
                )
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .where(t_pedido.c.CLIENTE_ID == cliente_id)
                .order_by(t_pedido.c.FECHA_CREACION.desc())
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def listar_por_repartidor(self, repartidor_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.DISTANCIA_KM,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_rest.c.LATITUD.label("RESTAURANTE_LATITUD"),    # ← nuevo
                    t_rest.c.LONGITUD.label("RESTAURANTE_LONGITUD"),  # ← nuevo
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_cliente.c.DIRECCION.label("CLIENTE_DIRECCION")
                )
                .join(t_rest,    t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .join(t_cliente, t_pedido.c.CLIENTE_ID     == t_cliente.c.ID)
                .where(t_pedido.c.REPARTIDOR_ID == repartidor_id)
                .where(t_pedido.c.ESTADO.in_([1, 2]))
                .order_by(t_pedido.c.FECHA_CREACION)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def listar_todos(self) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.DISTANCIA_KM,
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE")
                )
                .join(t_cliente, t_pedido.c.CLIENTE_ID     == t_cliente.c.ID)
                .join(t_rest,    t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .order_by(t_pedido.c.FECHA_CREACION.desc())
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def actualizar_estado(self, pedido_id: int, estado: int, fecha_entrega: str = None) -> bool:
        values = {"ESTADO": estado}
        if fecha_entrega:
            values["FECHA_ENTREGA"] = fecha_entrega
        with engine.begin() as conn:
            result = conn.execute(
                update(t_pedido).where(t_pedido.c.ID == pedido_id).values(**values)
            )
            return result.rowcount > 0

    def obtener_factura(self, pedido_id: int) -> Optional[dict]:
        with engine.connect() as conn:
            row = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.DISTANCIA_KM,
                    t_pedido.c.FECHA_CREACION,
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_rep.c.NOMBRE.label("REPARTIDOR_NOMBRE"),
                    t_rep.c.COSTO_KM_HABIL
                )
                .join(t_cliente, t_pedido.c.CLIENTE_ID     == t_cliente.c.ID)
                .join(t_rest,    t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .outerjoin(t_rep, t_pedido.c.REPARTIDOR_ID == t_rep.c.ID)
                .where(t_pedido.c.ID == pedido_id)
            ).mappings().first()
            if not row:
                return None

            items = conn.execute(
                select(
                    t_detalle.c.CANTIDAD,
                    t_detalle.c.PRECIO_UNITARIO,
                    t_combo.c.NOMBRE.label("COMBO_NOMBRE"),
                    t_combo.c.NUMERO_COMBO
                )
                .join(t_combo, t_detalle.c.COMBO_ID == t_combo.c.ID)
                .where(t_detalle.c.PEDIDO_ID == pedido_id)
            ).mappings().all()

            subtotal = sum(i["CANTIDAD"] * i["PRECIO_UNITARIO"] for i in items)
            costo_km = (row["COSTO_KM_HABIL"] or 1000) * row["DISTANCIA_KM"]
            return {
                "id_pedido":          row["ID"],
                "estado":             row["ESTADO"],
                "cliente":            row["CLIENTE_NOMBRE"],
                "restaurante":        row["RESTAURANTE_NOMBRE"],
                "repartidor":         row["REPARTIDOR_NOMBRE"],
                "fecha_creacion":     row["FECHA_CREACION"],
                "distancia_km":       row["DISTANCIA_KM"],
                "items":              [dict(i) for i in items],
                "subtotal":           subtotal,
                "costo_envio":        costo_km,
                "total":              subtotal + costo_km
            }

    def actualizar_estado(self, id_rep: int, estado: int) -> bool:
        return self.actualizar_campos(id_rep, {"estado": estado})

    def restaurante_mas_pedidos(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(t_rest.c.NOMBRE, func.count(t_pedido.c.ID).label("TOTAL"))
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .group_by(t_rest.c.ID)
                .order_by(func.count(t_pedido.c.ID).desc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}

    def restaurante_menos_pedidos(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(t_rest.c.NOMBRE, func.count(t_pedido.c.ID).label("TOTAL"))
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .group_by(t_rest.c.ID)
                .order_by(func.count(t_pedido.c.ID).asc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}

    def monto_por_restaurante(self) -> list:
        from sqlalchemy import func
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_rest.c.NOMBRE,
                    func.sum(t_detalle.c.CANTIDAD * t_detalle.c.PRECIO_UNITARIO).label("MONTO_TOTAL")
                )
                .join(t_detalle, t_detalle.c.PEDIDO_ID == t_pedido.c.ID)
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .group_by(t_rest.c.ID)
                .order_by(func.sum(t_detalle.c.CANTIDAD * t_detalle.c.PRECIO_UNITARIO).desc())
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def cliente_top(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(t_cliente.c.NOMBRE, func.count(t_pedido.c.ID).label("TOTAL"))
                .join(t_cliente, t_pedido.c.CLIENTE_ID == t_cliente.c.ID)
                .group_by(t_cliente.c.ID)
                .order_by(func.count(t_pedido.c.ID).desc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}

    def hora_pico(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(
                    func.strftime("%H", t_pedido.c.FECHA_CREACION).label("HORA"),
                    func.count(t_pedido.c.ID).label("TOTAL")
                )
                .group_by(func.strftime("%H", t_pedido.c.FECHA_CREACION))
                .order_by(func.count(t_pedido.c.ID).desc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}


    @staticmethod
    def _map(row) -> Pedido:
        return Pedido(
            id=row["ID"],
            cliente_id=row["CLIENTE_ID"],
            restaurante_id=row["RESTAURANTE_ID"],
            repartidor_id=row["REPARTIDOR_ID"],
            fecha_creacion=row["FECHA_CREACION"],
            fecha_entrega=row["FECHA_ENTREGA"],
            estado=row["ESTADO"],
            distancia_km=row["DISTANCIA_KM"]
        )

