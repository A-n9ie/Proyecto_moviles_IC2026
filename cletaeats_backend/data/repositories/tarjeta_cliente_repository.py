# data/repositories/tarjeta_cliente_repository.py
from sqlalchemy import select, insert, update, delete
from cletaeats_backend.core.entities import tarjeta_cliente
from data.database.db_connection import engine
from data.database.tables import tarjeta_cliente as t_tarjeta
from data.utils.mapper_utils import to_lower_dict

class TarjetaClienteRepository:

    def listar_por_cliente(self, cliente_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_tarjeta)
                .where(t_tarjeta.c.CLIENTE_ID == cliente_id)
                .order_by(t_tarjeta.c.ES_PRINCIPAL.desc(), t_tarjeta.c.ID)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def crear(self, data: dict) -> int:
        with engine.begin() as conn:
            if data.get("es_principal", 0):
                conn.execute(
                    update(t_tarjeta)
                    .where(t_tarjeta.c.CLIENTE_ID == data["cliente_id"])
                    .values(ES_PRINCIPAL=0)
                )
            result = conn.execute(insert(t_tarjeta).values(
                CLIENTE_ID=data["cliente_id"],
                NUMERO=data["numero"],
                ALIAS=data.get("alias", ""),
                FECHA_VENCIMIENTO=data.get("fecha_vencimiento", ""),
                CVV=data.get("cvv", ""),
                ES_PRINCIPAL=data.get("es_principal", 0)
            ))
            return result.inserted_primary_key[0]
        
    def obtener_por_id(self, id: int):
        with engine.connect() as conn:
            row = conn.execute(
                select(tarjeta_cliente).where(tarjeta_cliente.c.ID == id)
            ).mappings().first()
            return dict(row) if row else None

    def actualizar(self, data: dict) -> bool:
        from sqlalchemy import update
        with engine.begin() as conn:
            result = conn.execute(
                update(tarjeta_cliente).where(tarjeta_cliente.c.ID == data["id"]).values(
                    ALIAS             = data["alias"],
                    FECHA_VENCIMIENTO = data["fecha_vencimiento"],
                    CVV               = data["cvv"],
                    ES_PRINCIPAL      = data["es_principal"]
                )
            )
            return result.rowcount > 0

    def eliminar(self, id_tarjeta: int) -> bool:
        with engine.begin() as conn:
            result = conn.execute(
                delete(t_tarjeta).where(t_tarjeta.c.ID == id_tarjeta)
            )
            return result.rowcount > 0
