# data/repositories/combo_repository.py
from typing import List, Optional
from domain.interfaces.i_combo_repository import IComboRepository
from core.entities.combo import Combo
from data.database.db_connection import get_connection


class ComboRepository(IComboRepository):

    def obtener_por_restaurante(self, restaurante_id: int) -> List[Combo]:
        conn = get_connection()
        try:
            rows = conn.execute(
                """
                SELECT * FROM COMBO
                WHERE RESTAURANTE_ID = ? AND ESTADO = 1
                ORDER BY NUMERO_COMBO
                """,
                (restaurante_id,)
            ).fetchall()
            return [self._fila_a_combo(r) for r in rows]
        finally:
            conn.close()

    def obtener_por_id(self, combo_id: int) -> Optional[Combo]:
        conn = get_connection()
        try:
            row = conn.execute(
                "SELECT * FROM COMBO WHERE ID = ?", (combo_id,)
            ).fetchone()
            return self._fila_a_combo(row) if row else None
        finally:
            conn.close()

    @staticmethod
    def _fila_a_combo(row) -> Combo:
        return Combo(
            id             = row["ID"],
            restaurante_id = row["RESTAURANTE_ID"],
            numero_combo   = row["NUMERO_COMBO"],
            nombre         = row["NOMBRE"],
            descripcion    = row["DESCRIPCION"] or "",
            precio         = row["PRECIO"],
            imagen_url     = row["IMAGEN_URL"] or "",
            estado         = row["ESTADO"]
        )