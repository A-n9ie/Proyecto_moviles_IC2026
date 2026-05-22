# data/repositories/restaurante_repository.py
from typing import List, Optional
from sqlalchemy import select, insert, update
from data.database.db_connection import engine
from data.database.tables import (
    restaurante as t_rest, categoria as t_cat,
    restaurante_categoria as t_rc
)
from core.entities.restaurante import Restaurante
from data.utils.mapper_utils import to_lower_dict


class RestauranteRepository:

    def obtener_todos_activos(self) -> List[Restaurante]:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_rest).where(t_rest.c.ESTADO == 1).order_by(t_rest.c.NOMBRE)
            ).mappings().all()
            return [self._map(r) for r in rows]

    def obtener_por_id(self, id_restaurante: int) -> Optional[Restaurante]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_rest).where(t_rest.c.ID == id_restaurante)
            ).mappings().first()
            return self._map(row) if row else None

    def listar_todos(self) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_rest).order_by(t_rest.c.ID)
            ).mappings().all()

            result = []

            for r in rows:
                d = to_lower_dict(r)

                cats = conn.execute(
                    select(
                        t_cat.c.ID,
                        t_cat.c.NOMBRE
                    )
                    .join(t_rc, t_cat.c.ID == t_rc.c.CATEGORIA_ID)
                    .where(t_rc.c.RESTAURANTE_ID == d["id"])
                ).mappings().all()

                d["categorias"] = [
                    to_lower_dict(c) for c in cats
                ]

                result.append(d)

            return result

    def crear(self, data: dict) -> int:
        with engine.begin() as conn:
            result = conn.execute(insert(t_rest).values(
                CEDULA_JURIDICA=data["cedula_juridica"],
                NOMBRE=data["nombre"],
                DIRECCION=data["direccion"],
                IMAGEN_URL=data.get("imagen_url", ""),
                ESTADO=1
            ))
            return result.inserted_primary_key[0]

    def actualizar_campos(self, id_rest: int, data: dict) -> bool:
        allowed = {
            "nombre":     "NOMBRE",
            "direccion":  "DIRECCION",
            "imagen_url": "IMAGEN_URL",
            "estado":     "ESTADO"
        }
        values = {allowed[k]: v for k, v in data.items() if k in allowed}
        if not values:
            return False
        with engine.begin() as conn:
            result = conn.execute(
                update(t_rest).where(t_rest.c.ID == id_rest).values(**values)
            )
            return result.rowcount > 0

    @staticmethod
    def _map(row) -> Restaurante:
        return Restaurante(
            id=row["ID"],
            cedula_juridica=row.get("CEDULA_JURIDICA", ""),
            nombre=row["NOMBRE"],
            direccion=row["DIRECCION"],
            estado=row["ESTADO"],
            imagen_url=row["IMAGEN_URL"] or ""
        )
    