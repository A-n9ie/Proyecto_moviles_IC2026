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

    def listar_todos(self) -> list:
        conn = get_connection()
        try:
            rows = conn.execute("""
                SELECT
                    ID                AS id,
                    NOMBRE            AS nombre,
                    TIPO_COMIDA       AS tipo_comida,
                    DIRECCION         AS direccion,
                    IMAGEN_URL        AS imagen_url,
                    ESTADO            AS estado
                FROM RESTAURANTE
                ORDER BY ID
            """).fetchall()

            return [dict(r) for r in rows]
        finally:
            conn.close()

    def crear(self, data: dict) -> int:
        conn = get_connection()
        try:
            cur = conn.execute(
                "INSERT INTO RESTAURANTE (NOMBRE, TIPO_COMIDA, DIRECCION, IMAGEN_URL, ESTADO) VALUES (?,?,?,?,1)",
                (data["nombre"], data["tipo_comida"], data["direccion"], data.get("imagen_url",""))
            )
            conn.commit()
            return cur.lastrowid
        finally:
            conn.close()

    def actualizar_campos(self, id_rest: int, data: dict) -> bool:
        campos = []
        vals   = []
        for k, col in [("nombre","NOMBRE"),("tipo_comida","TIPO_COMIDA"),("direccion","DIRECCION"),("imagen_url","IMAGEN_URL"),("estado","ESTADO")]:
            if k in data:
                campos.append(f"{col} = ?")
                vals.append(data[k])
        if not campos: return False
        vals.append(id_rest)
        conn = get_connection()
        try:
            cur = conn.execute(f"UPDATE RESTAURANTE SET {', '.join(campos)} WHERE ID = ?", vals)
            conn.commit()
            return cur.rowcount > 0
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
    