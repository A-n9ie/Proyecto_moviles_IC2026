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

    def listar_todos(self) -> list:
        conn = get_connection()
        try:
            rows = conn.execute("""
                SELECT
                    C.ID                    AS id,
                    C.RESTAURANTE_ID        AS restaurante_id,
                    R.NOMBRE                AS restaurante_nombre,
                    C.NUMERO_COMBO          AS numero_combo,
                    C.NOMBRE                AS nombre,
                    C.DESCRIPCION           AS descripcion,
                    C.PRECIO                AS precio,
                    C.IMAGEN_URL            AS imagen_url,
                    C.ESTADO                AS estado
                FROM COMBO C
                JOIN RESTAURANTE R
                    ON C.RESTAURANTE_ID = R.ID
                ORDER BY C.ID
            """).fetchall()
            return [dict(r) for r in rows]
        finally:
            conn.close()

    def crear(self, data: dict) -> int:
        conn = get_connection()
        try:
            cur = conn.execute(
                "INSERT INTO COMBO (RESTAURANTE_ID, NUMERO_COMBO, NOMBRE, DESCRIPCION, PRECIO, ESTADO) VALUES (?,?,?,?,?,1)",
                (data["restaurante_id"], data["numero_combo"], data["nombre"], data.get("descripcion",""), data["precio"])
            )
            conn.commit()
            return cur.lastrowid
        finally:
            conn.close()

    def actualizar_campos(self, id_combo: int, data: dict) -> bool:
        campos = []
        vals   = []
        for k, col in [("nombre","NOMBRE"),("descripcion","DESCRIPCION"),("precio","PRECIO"),("estado","ESTADO")]:
            if k in data:
                campos.append(f"{col} = ?")
                vals.append(data[k])
        if not campos: return False
        vals.append(id_combo)
        conn = get_connection()
        try:
            cur = conn.execute(f"UPDATE COMBO SET {', '.join(campos)} WHERE ID = ?", vals)
            conn.commit()
            return cur.rowcount > 0
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
