# data/repositories/combo_repository.py
from typing import List, Optional
from sqlalchemy import select, insert, update, delete
from data.database.db_connection import engine
from data.database.tables import (
    combo as t_combo, restaurante as t_rest,
    combo_producto as t_cp, producto as t_prod,
    detalle_pedido as t_dp
)
from core.entities.combo import Combo
from data.utils.mapper_utils import to_lower_dict


class ComboRepository:

    def obtener_por_restaurante(self, restaurante_id: int) -> List[Combo]:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_combo)
                .where(t_combo.c.RESTAURANTE_ID == restaurante_id)
                .where(t_combo.c.ESTADO == 1)
                .order_by(t_combo.c.NUMERO_COMBO)
            ).mappings().all()
            return [self._map(r) for r in rows]

    def obtener_por_id(self, combo_id: int) -> Optional[Combo]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_combo).where(t_combo.c.ID == combo_id)
            ).mappings().first()
            return self._map(row) if row else None

    def listar_todos(self) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_combo.c.ID,
                    t_combo.c.RESTAURANTE_ID,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_combo.c.NUMERO_COMBO,
                    t_combo.c.NOMBRE,
                    t_combo.c.DESCRIPCION,
                    t_combo.c.PRECIO,
                    t_combo.c.IMAGEN_URL,
                    t_combo.c.ESTADO
                )
                .join(t_rest, t_combo.c.RESTAURANTE_ID == t_rest.c.ID)
                .order_by(t_combo.c.ID)
            ).mappings().all()
            result = []
            for r in rows:
                d = to_lower_dict(r)
                # Incluir productos del combo
                prods = conn.execute(
                    select(t_prod.c.ID.label("id"), t_prod.c.NOMBRE.label("nombre"))
                    .join(t_cp, t_prod.c.ID == t_cp.c.PRODUCTO_ID)
                    .where(t_cp.c.COMBO_ID == d["id"])
                ).mappings().all()
                d["productos"] = [dict(p) for p in prods]
                result.append(d)
            return [to_lower_dict(r) for r in rows]

    def crear(self, data: dict) -> int:
        with engine.begin() as conn:
            result = conn.execute(insert(t_combo).values(
                RESTAURANTE_ID=data["restaurante_id"],
                NUMERO_COMBO=data["numero_combo"],
                NOMBRE=data["nombre"],
                DESCRIPCION=data.get("descripcion", ""),
                PRECIO=data["precio"],
                IMAGEN_URL=data.get("imagen_url", ""),
            ))
            combo_id = result.inserted_primary_key[0]
            for pid in data.get("producto_ids", []):
                conn.execute(insert(t_cp).values(COMBO_ID=combo_id, PRODUCTO_ID=pid))
            return combo_id

    def actualizar_campos(self, id_combo: int, data: dict) -> bool:
        allowed = {
            "nombre": "NOMBRE", "descripcion": "DESCRIPCION",
            "precio": "PRECIO", "estado": "ESTADO",
            "imagen_url": "IMAGEN_URL",
        }
        values = {allowed[k]: v for k, v in data.items() if k in allowed}
        with engine.begin() as conn:
            if values:
                result = conn.execute(
                    update(t_combo).where(t_combo.c.ID == id_combo).values(**values)
                )
                if result.rowcount == 0:
                    return False
            if "producto_ids" in data:
                conn.execute(delete(t_cp).where(t_cp.c.COMBO_ID == id_combo))
                for pid in data["producto_ids"]:
                    conn.execute(insert(t_cp).values(COMBO_ID=id_combo, PRODUCTO_ID=pid))
        return True

    def eliminar(self, id_combo: int) -> bool:
        with engine.connect() as conn:
            if conn.execute(select(t_dp.c.ID).where(t_dp.c.COMBO_ID == id_combo).limit(1)).first():
                raise ValueError("No se puede eliminar el combo porque está enlazado a pedidos")

        with engine.begin() as conn:
            result = conn.execute(
                delete(t_combo).where(t_combo.c.ID == id_combo)
            )
            return result.rowcount > 0

    @staticmethod
    def _map(row) -> Combo:
        return Combo(
            id=row["ID"],
            restaurante_id=row["RESTAURANTE_ID"],
            numero_combo=row["NUMERO_COMBO"],
            nombre=row["NOMBRE"],
            descripcion=row["DESCRIPCION"] or "",
            precio=row["PRECIO"],
            imagen_url=row["IMAGEN_URL"] or "",
            estado=row["ESTADO"]
        )