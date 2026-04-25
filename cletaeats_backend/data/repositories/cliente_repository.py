# data/repositories/cliente_repository.py
from typing import Optional
from domain.interfaces.i_cliente_repository import IClienteRepository
from core.entities.cliente import Cliente
from data.database.db_connection import get_connection


class ClienteRepository(IClienteRepository):

    def crear(self, cliente: Cliente) -> Cliente:
        conn = get_connection()
        try:
            cursor = conn.execute(
                """
                INSERT INTO CLIENTE (USUARIO_ID, CEDULA, NOMBRE, DIRECCION, TELEFONO, TARJETA)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                (
                    cliente.usuario_id, cliente.cedula, cliente.nombre,
                    cliente.direccion, cliente.telefono, cliente.tarjeta
                )
            )
            conn.commit()
            cliente.id = cursor.lastrowid
            return cliente
        finally:
            conn.close()

    def encontrar_por_usuario_id(self, usuario_id: int) -> Optional[Cliente]:
        conn = get_connection()
        try:
            row = conn.execute(
                "SELECT * FROM CLIENTE WHERE USUARIO_ID = ?", (usuario_id,)
            ).fetchone()
            return self._fila_a_cliente(row) if row else None
        finally:
            conn.close()

    def existe_cedula(self, cedula: str) -> bool:
        conn = get_connection()
        try:
            count = conn.execute(
                "SELECT COUNT(*) FROM CLIENTE WHERE CEDULA = ?", (cedula,)
            ).fetchone()[0]
            return count > 0
        finally:
            conn.close()

    @staticmethod
    def _fila_a_cliente(row) -> Cliente:
        return Cliente(
            id=row["ID"],
            usuario_id=row["USUARIO_ID"],
            cedula=row["CEDULA"],
            nombre=row["NOMBRE"],
            direccion=row["DIRECCION"],
            telefono=row["TELEFONO"],
            tarjeta=row["TARJETA"]
        )