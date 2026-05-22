# data/repositories/tarjeta_cliente_repository.py
from sqlalchemy import select, insert, update, delete
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
                ES_PRINCIPAL=data.get("es_principal", 0)
            ))
            return result.inserted_primary_key[0]

    def eliminar(self, id_tarjeta: int) -> bool:
        with engine.begin() as conn:
            result = conn.execute(
                delete(t_tarjeta).where(t_tarjeta.c.ID == id_tarjeta)
            )
            return result.rowcount > 0
