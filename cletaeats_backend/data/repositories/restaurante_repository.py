# data/repositories/restaurante_repository.py
from typing import List, Optional
from domain.interfaces.i_restaurante_repository import IRestauranteRepository
from core.entities.restaurante import Restaurante
from data.database.db_connection import get_connection


class RestauranteRepository(IRestauranteRepository):

    def obtener_todos_activos(self) -> List[Restaurante]:
        conn = get_connection()
        try:
            rows = conn.execute(
                "SELECT * FROM RESTAURANTE WHERE ESTADO = 1 ORDER BY NOMBRE"
            ).fetchall()
            return [self._fila_a_restaurante(r) for r in rows]
        finally:
            conn.close()

    def obtener_por_id(self, id_restaurante: int) -> Optional[Restaurante]:
        conn = get_connection()
        try:
            row = conn.execute(
                "SELECT * FROM RESTAURANTE WHERE ID = ?", (id_restaurante,)
            ).fetchone()
            return self._fila_a_restaurante(row) if row else None
        finally:
            conn.close()

    @staticmethod
    def _fila_a_restaurante(row) -> Restaurante:
        return Restaurante(
            id=row["ID"],
            cedula_juridica=row["CEDULA_JURIDICA"],
            nombre=row["NOMBRE"],
            direccion=row["DIRECCION"],
            tipo_comida=row["TIPO_COMIDA"],
            estado=row["ESTADO"],
            imagen_url=row["IMAGEN_URL"] if row["IMAGEN_URL"] else ""
        )