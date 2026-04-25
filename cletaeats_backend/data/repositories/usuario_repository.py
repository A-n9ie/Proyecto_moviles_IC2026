# data/repositories/usuario_repository.py
from typing import Optional
from domain.interfaces.i_usuario_repository import IUsuarioRepository
from core.entities.usuario import Usuario
from data.database.db_connection import get_connection


class UsuarioRepository(IUsuarioRepository):

    def encontrar_por_email(self, email: str) -> Optional[Usuario]:
        conn = get_connection()
        try:
            row = conn.execute(
                "SELECT * FROM USUARIO WHERE EMAIL = ?", (email,)
            ).fetchone()
            return self._fila_a_usuario(row) if row else None
        finally:
            conn.close()

    def crear(self, usuario: Usuario) -> Usuario:
        conn = get_connection()
        try:
            cursor = conn.execute(
                """
                INSERT INTO USUARIO (EMAIL, PASSWORD_HASH, ROL, ESTADO)
                VALUES (?, ?, ?, ?)
                """,
                (usuario.email, usuario.password_hash, usuario.rol, usuario.estado)
            )
            conn.commit()
            usuario.id = cursor.lastrowid
            return usuario
        finally:
            conn.close()

    def existe_email(self, email: str) -> bool:
        conn = get_connection()
        try:
            count = conn.execute(
                "SELECT COUNT(*) FROM USUARIO WHERE EMAIL = ?", (email,)
            ).fetchone()[0]
            return count > 0
        finally:
            conn.close()

    @staticmethod
    def _fila_a_usuario(row) -> Usuario:
        return Usuario(
            id=row["ID"],
            email=row["EMAIL"],
            password_hash=row["PASSWORD_HASH"],
            rol=row["ROL"],
            estado=row["ESTADO"],
            fecha_registro=row["FECHA_REGISTRO"]
        )