from data.database.db_connection import engine
from data.database.tables import bitacora as t_bit
from sqlalchemy import insert, select, desc

class BitacoraRepository:

    def registrar(self, usuario_id, rol: str, accion: str, detalle: str = ""):
        try:
            with engine.begin() as conn:
                conn.execute(insert(t_bit).values(
                    USUARIO_ID = usuario_id,
                    ROL        = rol,
                    ACCION     = accion,
                    DETALLE    = detalle or ""
                ))
        except Exception:
            pass  # nunca romper el flujo principal por la bitácora

    def listar(self, limit: int = 200) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(t_bit).order_by(desc(t_bit.c.FECHA)).limit(limit)
            ).mappings().all()
        return [dict(r) for r in rows]
