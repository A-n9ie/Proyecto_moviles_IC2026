# data/repositories/usuario_repository.py
from typing import Optional
from domain.interfaces.i_usuario_repository import IUsuarioRepository
from core.entities.usuario import Usuario
from data.database.db_connection import get_connection


class UsuarioRepository(IUsuarioRepository):
    # Implementación SQLite de IUsuarioRepository.

    def encontrar_por_nombre_usuario(self, nombre_usuario: str) -> Optional[Usuario]:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute(
                """
                SELECT ID_USUARIO, NOMBRE_USUARIO, EMAIL, PASSWORD, ESTADO_USUARIO
                FROM USUARIO
                WHERE NOMBRE_USUARIO = ?
                """,
                (nombre_usuario,)
            )
            row = cursor.fetchone()
            return self._fila_a_usuario(row) if row else None
        finally:
            conn.close()

    def crear(self, usuario: Usuario) -> Usuario:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute(
                """
                INSERT INTO USUARIO (NOMBRE_USUARIO, EMAIL, PASSWORD, ESTADO_USUARIO)
                VALUES (?, ?, ?, ?)
                """,
                (
                    usuario.nombre_usuario,
                    usuario.email,
                    usuario.password,
                    usuario.estado_usuario
                )
            )
            conn.commit()
            usuario.id = cursor.lastrowid
            return usuario
        finally:
            conn.close()

    def existe(self, nombre_usuario: str) -> bool:
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT COUNT(*) FROM USUARIO WHERE NOMBRE_USUARIO = ?",
                (nombre_usuario,)
            )
            return cursor.fetchone()[0] > 0
        finally:
            conn.close()

    @staticmethod
    def _fila_a_usuario(row) -> Usuario:
        return Usuario(
            id=row[0],
            nombre_usuario=row[1],
            email=row[2],
            password=row[3],
            estado_usuario=row[4]
        )