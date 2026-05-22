# data/repositories/usuario_repository.py
from typing import Optional
from sqlalchemy import select, insert, update
from data.database.db_connection import engine
from data.database.tables import usuario as t_usuario
from core.entities.usuario import Usuario


class UsuarioRepository:

    def encontrar_por_email(self, email: str) -> Optional[Usuario]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_usuario).where(t_usuario.c.EMAIL == email)
            ).mappings().first()
            return self._map(row) if row else None

    def existe_email(self, email: str) -> bool:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_usuario.c.ID).where(t_usuario.c.EMAIL == email)
            ).first()
            return row is not None

    def crear(self, u: Usuario) -> Usuario:
        with engine.begin() as conn:
            result = conn.execute(insert(t_usuario).values(
                EMAIL=u.email,
                PASSWORD_HASH=u.password_hash,
                ROL=u.rol,
                ESTADO=u.estado
            ))
            u.id = result.inserted_primary_key[0]
            return u

    def actualizar_estado(self, usuario_id: int, estado: int) -> bool:
        with engine.begin() as conn:
            result = conn.execute(
                update(t_usuario)
                .where(t_usuario.c.ID == usuario_id)
                .values(ESTADO=estado)
            )
            return result.rowcount > 0

    @staticmethod
    def _map(row) -> Usuario:
        return Usuario(
            id=row["ID"],
            email=row["EMAIL"],
            password_hash=row["PASSWORD_HASH"],
            rol=row["ROL"],
            estado=row["ESTADO"],
            fecha_registro=row["FECHA_REGISTRO"]
        )