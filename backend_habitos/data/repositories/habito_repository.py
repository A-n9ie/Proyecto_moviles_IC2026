# data/repositories/habito_repository.py
from typing import List, Optional
from domain.interfaces.i_habito_repository import IHabitoRepository
from core.entities.habito import Habito
from data.database.db_connection import get_connection


class HabitoRepository(IHabitoRepository):
    # Implementación SQLite de IHabitoRepository.

    def obtener_por_usuario(self, id_usuario: int) -> List[Habito]:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute(
                """
                SELECT ID_HABITO, NOMBRE_HABITO, DESCRIPCION, ID_USUARIO,
                       ID_CODIGO_TIPO_HABITO, ESTADO_HABITO, DURACION_OBJETIVO
                FROM HABITO
                WHERE ID_USUARIO = ?
                """,
                (id_usuario,)
            )
            return [self._fila_a_habito(r) for r in cursor.fetchall()]
        finally:
            conn.close()

    def obtener_por_id(self, id_habito: int) -> Optional[Habito]:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute(
                """
                SELECT ID_HABITO, NOMBRE_HABITO, DESCRIPCION, ID_USUARIO,
                       ID_CODIGO_TIPO_HABITO, ESTADO_HABITO, DURACION_OBJETIVO
                FROM HABITO
                WHERE ID_HABITO = ?
                """,
                (id_habito,)
            )
            row = cursor.fetchone()
            return self._fila_a_habito(row) if row else None
        finally:
            conn.close()

    def crear(self, habito: Habito) -> Habito:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute(
                """
                INSERT INTO HABITO
                    (NOMBRE_HABITO, DESCRIPCION, ID_USUARIO,
                     ID_CODIGO_TIPO_HABITO, ESTADO_HABITO, DURACION_OBJETIVO)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                (
                    habito.nombre,
                    habito.descripcion,
                    habito.id_usuario,
                    habito.id_codigo_tipo_habito,
                    habito.estado_habito,
                    habito.duracion_objetivo
                )
            )
            conn.commit()
            habito.id = cursor.lastrowid
            return habito
        finally:
            conn.close()

    def actualizar(self, habito: Habito) -> Habito:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute(
                """
                UPDATE HABITO
                SET NOMBRE_HABITO      = ?,
                    DESCRIPCION        = ?,
                    ID_CODIGO_TIPO_HABITO  = ?,
                    ESTADO_HABITO  = ?,
                    DURACION_OBJETIVO  = ?
                WHERE ID_HABITO = ?
                """,
                (
                    habito.nombre,
                    habito.descripcion,
                    habito.id_codigo_tipo_habito,
                    habito.estado_habito,
                    habito.duracion_objetivo,
                    habito.id
                )
            )
            conn.commit()
            return habito
        finally:
            conn.close()

    def eliminar(self, id_habito: int) -> bool:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM HABITO WHERE ID_HABITO = ?", (id_habito,))
            conn.commit()
            return cursor.rowcount > 0
        finally:
            conn.close()

    @staticmethod
    def _fila_a_habito(row) -> Habito:
        return Habito(
            id=row[0],
            nombre=row[1],
            descripcion=row[2] or "",
            id_usuario=row[3],
            id_codigo_tipo_habito=row[4],
            estado_habito=row[5],
            duracion_objetivo=row[6]
        )