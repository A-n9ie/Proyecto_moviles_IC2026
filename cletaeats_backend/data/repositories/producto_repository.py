# data/repositories/producto_repository.py
from sqlalchemy import select, insert, update, delete
from data.database.db_connection import engine
from data.database.tables import producto as t_prod, restaurante as t_rest, combo_producto as t_cp
from data.utils.mapper_utils import to_lower_dict

class ProductoRepository:

    def listar_por_restaurante(self, restaurante_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_prod)
                .where(t_prod.c.RESTAURANTE_ID == restaurante_id)
                .order_by(t_prod.c.NOMBRE)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def listar_todos(self) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_prod.c.ID,
                    t_prod.c.RESTAURANTE_ID,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_prod.c.NOMBRE,
                    t_prod.c.DESCRIPCION,
                    t_prod.c.ESTADO
                )
                .join(t_rest, t_prod.c.RESTAURANTE_ID == t_rest.c.ID)
                .order_by(t_rest.c.NOMBRE, t_prod.c.NOMBRE)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def crear(self, data: dict) -> int:
        with engine.begin() as conn:
            result = conn.execute(insert(t_prod).values(
                RESTAURANTE_ID=data["restaurante_id"],
                NOMBRE=data["nombre"],
                DESCRIPCION=data.get("descripcion", "")
            ))
            return result.inserted_primary_key[0]

    def actualizar_campos(self, id_producto: int, data: dict) -> bool:
        allowed = {"nombre": "NOMBRE", "descripcion": "DESCRIPCION", "estado": "ESTADO"}
        values = {allowed[k]: v for k, v in data.items() if k in allowed}
        if not values:
            return False
        with engine.begin() as conn:
            result = conn.execute(
                update(t_prod).where(t_prod.c.ID == id_producto).values(**values)
            )
            return result.rowcount > 0

    def asignar_a_combo(self, combo_id: int, producto_ids: list) -> None:
        with engine.begin() as conn:
            conn.execute(delete(t_cp).where(t_cp.c.COMBO_ID == combo_id))
            for pid in producto_ids:
                conn.execute(insert(t_cp).values(COMBO_ID=combo_id, PRODUCTO_ID=pid))

    def obtener_por_combo(self, combo_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_prod.c.ID.label("id"),
                    t_prod.c.NOMBRE.label("nombre"),
                    t_prod.c.DESCRIPCION.label("descripcion")
                )
                .join(t_cp, t_prod.c.ID == t_cp.c.PRODUCTO_ID)
                .where(t_cp.c.COMBO_ID == combo_id)
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]
