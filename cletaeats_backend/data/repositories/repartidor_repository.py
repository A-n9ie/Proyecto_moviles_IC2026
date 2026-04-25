# data/repositories/repartidor_repository.py
from typing import Optional
from domain.interfaces.i_repartidor_repository import IRepartidorRepository
from core.entities.repartidor import Repartidor
from data.database.db_connection import get_connection


class RepartidorRepository(IRepartidorRepository):

    def crear(self, r: Repartidor) -> Repartidor:
        conn = get_connection()
        try:
            cursor = conn.execute(
                """
                INSERT INTO REPARTIDOR
                    (USUARIO_ID, CEDULA, NOMBRE, CORREO, DIRECCION, TELEFONO, TARJETA)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (r.usuario_id, r.cedula, r.nombre, r.correo,
                 r.direccion, r.telefono, r.tarjeta)
            )
            conn.commit()
            r.id = cursor.lastrowid
            return r
        finally:
            conn.close()

    def encontrar_por_usuario_id(self, usuario_id: int) -> Optional[Repartidor]:
        conn = get_connection()
        try:
            row = conn.execute(
                "SELECT * FROM REPARTIDOR WHERE USUARIO_ID = ?", (usuario_id,)
            ).fetchone()
            return self._fila_a_repartidor(row) if row else None
        finally:
            conn.close()

    def existe_cedula(self, cedula: str) -> bool:
        conn = get_connection()
        try:
            return conn.execute(
                "SELECT COUNT(*) FROM REPARTIDOR WHERE CEDULA = ?", (cedula,)
            ).fetchone()[0] > 0
        finally:
            conn.close()

    def obtener_primero_disponible(self) -> Optional[Repartidor]:
        conn = get_connection()
        try:
            row = conn.execute(
                """
                SELECT * FROM REPARTIDOR
                WHERE ESTADO = 1 AND AMONESTACIONES < 4
                ORDER BY ID
                LIMIT 1
                """
            ).fetchone()
            return self._fila_a_repartidor(row) if row else None
        finally:
            conn.close()

    @staticmethod
    def _fila_a_repartidor(row) -> Repartidor:
        return Repartidor(
            id=row["ID"],
            usuario_id=row["USUARIO_ID"],
            cedula=row["CEDULA"],
            nombre=row["NOMBRE"],
            correo=row["CORREO"],
            direccion=row["DIRECCION"],
            telefono=row["TELEFONO"],
            tarjeta=row["TARJETA"],
            estado=row["ESTADO"],
            km_recorridos_diarios=row["KM_RECORRIDOS_DIARIOS"],
            costo_km_habil=row["COSTO_KM_HABIL"],
            costo_km_feriado=row["COSTO_KM_FERIADO"],
            amonestaciones=row["AMONESTACIONES"]
        )