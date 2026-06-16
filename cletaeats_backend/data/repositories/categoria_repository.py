# data/repositories/categoria_repository.py
from sqlalchemy import select, insert, delete
from data.database.db_connection import engine
from data.database.tables import categoria as t_cat, restaurante_categoria as t_rc
from data.utils.mapper_utils import to_lower_dict

class CategoriaRepository:

    def listar_todas(self) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_cat).order_by(t_cat.c.NOMBRE)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def crear(self, nombre: str) -> int:
        with engine.begin() as conn:
            result = conn.execute(insert(t_cat).values(NOMBRE=nombre))
            return result.inserted_primary_key[0]

    def eliminar(self, id_cat: int) -> bool:
        with engine.connect() as conn:
            if conn.execute(select(t_rc.c.RESTAURANTE_ID).where(t_rc.c.CATEGORIA_ID == id_cat).limit(1)).first():
                raise ValueError("No se puede eliminar la categoría porque está enlazada a restaurantes")

        with engine.begin() as conn:
            result = conn.execute(
                delete(t_cat).where(t_cat.c.ID == id_cat)
            )
            return result.rowcount > 0

    def asignar_a_restaurante(self, restaurante_id: int, categoria_ids: list) -> None:
        with engine.begin() as conn:
            conn.execute(
                delete(t_rc).where(t_rc.c.RESTAURANTE_ID == restaurante_id)
            )
            for cid in categoria_ids:
                conn.execute(insert(t_rc).values(
                    RESTAURANTE_ID=restaurante_id, CATEGORIA_ID=cid
                ))

    def obtener_por_restaurante(self, restaurante_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_cat.c.ID.label("id"), t_cat.c.NOMBRE.label("nombre"))
                .join(t_rc, t_cat.c.ID == t_rc.c.CATEGORIA_ID)
                .where(t_rc.c.RESTAURANTE_ID == restaurante_id)
                .order_by(t_cat.c.NOMBRE)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]