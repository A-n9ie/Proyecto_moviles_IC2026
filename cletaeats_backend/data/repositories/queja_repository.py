# data/repositories/queja_repository.py
from typing import Optional
from sqlalchemy import select, insert, update, func
from data.database.db_connection import engine
from data.database.tables import (
    queja as t_queja,
    cliente as t_cliente,
    repartidor as t_rep,
    pedido as t_pedido,
)
from core.entities.queja import Queja


class QuejaRepository:

    def crear(self, queja: Queja) -> Queja:
        """Registra una nueva queja (estado PENDIENTE por defecto)."""
        with engine.begin() as conn:
            result = conn.execute(insert(t_queja).values(
                CLIENTE_ID=queja.cliente_id,
                REPARTIDOR_ID=queja.repartidor_id,
                PEDIDO_ID=queja.pedido_id,
                MOTIVO=queja.motivo,
                DESCRIPCION=queja.descripcion,
                ESTADO=0
            ))
            queja.id = result.inserted_primary_key[0]
            queja.estado = 0
        return queja

    def obtener_por_id(self, queja_id: int) -> Optional[Queja]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_queja).where(t_queja.c.ID == queja_id)
            ).mappings().first()
            return self._map(row) if row else None

    def listar_todas(self, estado: Optional[int] = None) -> list:
        """
        Lista todas las quejas con nombres de cliente y repartidor.
        Si se pasa 'estado', filtra por ese estado (0/1/2).
        """
        with engine.connect() as conn:
            stmt = (
                select(
                    t_queja.c.ID,
                    t_queja.c.MOTIVO,
                    t_queja.c.DESCRIPCION,
                    t_queja.c.FECHA,
                    t_queja.c.ESTADO,
                    t_queja.c.PEDIDO_ID,
                    t_queja.c.CLIENTE_ID,
                    t_queja.c.REPARTIDOR_ID,
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_rep.c.NOMBRE.label("REPARTIDOR_NOMBRE"),
                )
                .join(t_cliente, t_queja.c.CLIENTE_ID == t_cliente.c.ID)
                .join(t_rep, t_queja.c.REPARTIDOR_ID == t_rep.c.ID)
                .order_by(t_queja.c.FECHA.desc())
            )
            if estado is not None:
                stmt = stmt.where(t_queja.c.ESTADO == estado)
            rows = conn.execute(stmt).mappings().all()
            return [self._to_dict(r) for r in rows]

    def actualizar_estado(self, queja_id: int, estado: int) -> bool:
        """Cambia el estado de la queja (0=pendiente, 1=amonestada, 2=menor)."""
        with engine.begin() as conn:
            result = conn.execute(
                update(t_queja).where(t_queja.c.ID == queja_id).values(ESTADO=estado)
            )
            return result.rowcount > 0

    def quejas_por_repartidor(self) -> list:
        """
        Reporte (m): listado de quejas agrupadas por repartidor.
        Cada repartidor trae su lista de quejas y el conteo total.
        """
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_rep.c.ID.label("REPARTIDOR_ID"),
                    t_rep.c.NOMBRE.label("REPARTIDOR_NOMBRE"),
                    t_rep.c.CEDULA.label("REPARTIDOR_CEDULA"),
                    t_rep.c.AMONESTACIONES,
                    t_queja.c.ID.label("QUEJA_ID"),
                    t_queja.c.MOTIVO,
                    t_queja.c.DESCRIPCION,
                    t_queja.c.FECHA,
                    t_queja.c.ESTADO,
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                )
                .join(t_queja, t_queja.c.REPARTIDOR_ID == t_rep.c.ID)
                .join(t_cliente, t_queja.c.CLIENTE_ID == t_cliente.c.ID)
                .order_by(t_rep.c.NOMBRE, t_queja.c.FECHA.desc())
            ).mappings().all()

            agrupado = {}
            for r in rows:
                rid = r["REPARTIDOR_ID"]
                if rid not in agrupado:
                    agrupado[rid] = {
                        "repartidor_id":     rid,
                        "repartidor_nombre": r["REPARTIDOR_NOMBRE"],
                        "repartidor_cedula": r["REPARTIDOR_CEDULA"],
                        "amonestaciones":    r["AMONESTACIONES"],
                        "total_quejas":      0,
                        "quejas":            []
                    }
                agrupado[rid]["quejas"].append({
                    "queja_id":      r["QUEJA_ID"],
                    "motivo":        r["MOTIVO"],
                    "descripcion":   r["DESCRIPCION"],
                    "fecha":         r["FECHA"],
                    "estado":        r["ESTADO"],
                    "estado_texto":  Queja.ESTADOS.get(r["ESTADO"], "DESCONOCIDO"),
                    "cliente":       r["CLIENTE_NOMBRE"],
                })
                agrupado[rid]["total_quejas"] += 1

            return list(agrupado.values())

    @staticmethod
    def _to_dict(row) -> dict:
        return {
            "id":                row["ID"],
            "motivo":            row["MOTIVO"],
            "descripcion":       row["DESCRIPCION"],
            "fecha":             row["FECHA"],
            "estado":            row["ESTADO"],
            "estado_texto":      Queja.ESTADOS.get(row["ESTADO"], "DESCONOCIDO"),
            "pedido_id":         row["PEDIDO_ID"],
            "cliente_id":        row["CLIENTE_ID"],
            "cliente_nombre":    row["CLIENTE_NOMBRE"],
            "repartidor_id":     row["REPARTIDOR_ID"],
            "repartidor_nombre": row["REPARTIDOR_NOMBRE"],
        }

    @staticmethod
    def _map(row) -> Queja:
        return Queja(
            id=row["ID"],
            cliente_id=row["CLIENTE_ID"],
            repartidor_id=row["REPARTIDOR_ID"],
            pedido_id=row["PEDIDO_ID"],
            motivo=row["MOTIVO"],
            descripcion=row["DESCRIPCION"],
            fecha=row["FECHA"],
            estado=row["ESTADO"],
        )