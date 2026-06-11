# data/repositories/repartidor_repository.py
from typing import Optional
from sqlalchemy import select, insert, update
from data.database.db_connection import engine
from data.database.tables import repartidor as t_rep, usuario as t_usuario
from core.entities.repartidor import Repartidor
from data.utils.mapper_utils import to_lower_dict

class RepartidorRepository:

    def crear(self, r: Repartidor) -> Repartidor:
        with engine.begin() as conn:
            result = conn.execute(insert(t_rep).values(
                USUARIO_ID=r.usuario_id,
                CEDULA=r.cedula,
                NOMBRE=r.nombre,
                CORREO=r.correo,
                DIRECCION=r.direccion,
                TELEFONO=r.telefono,
                TARJETA=r.tarjeta
            ))
            r.id = result.inserted_primary_key[0]
            return r

    def encontrar_por_usuario_id(self, usuario_id: int):
        with engine.connect() as conn:
            row = conn.execute(
                select(t_rep).where(t_rep.c.USUARIO_ID == usuario_id)
            ).mappings().first()
            return self._map(row) if row else None
        
    def obtener_por_id(self, repartidor_id: int) -> Optional[Repartidor]:
     with engine.connect() as conn:
        row = conn.execute(
            select(t_rep).where(t_rep.c.ID == repartidor_id)
        ).mappings().first()
        return self._map(row) if row else None

    def existe_cedula(self, cedula: str) -> bool:
        with engine.connect() as conn:
            return conn.execute(
                select(t_rep.c.ID).where(t_rep.c.CEDULA == cedula)
            ).first() is not None

    def obtener_primero_disponible(self) -> Optional[Repartidor]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_rep)
                .where(t_rep.c.ESTADO == 1)
                .where(t_rep.c.AMONESTACIONES < 4)
                .order_by(t_rep.c.ID)
                .limit(1)
            ).mappings().first()
            return self._map(row) if row else None

    def listar_todos(self) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_rep.c.ID,
                    t_rep.c.NOMBRE,
                    t_rep.c.CORREO,
                    t_rep.c.TELEFONO,
                    t_rep.c.CEDULA,
                    t_rep.c.DISPONIBLE,
                    t_rep.c.KM_RECORRIDOS_DIARIOS,
                    t_rep.c.AMONESTACIONES,
                    t_rep.c.USUARIO_ID,
                    t_usuario.c.ESTADO.label("ESTADO")
                )
                .join(t_usuario, t_rep.c.USUARIO_ID == t_usuario.c.ID)
                .order_by(t_rep.c.ID)
            ).mappings().all()

            return [to_lower_dict(r) for r in rows]

    def actualizar_campos(self, id_rep: int, data: dict) -> bool:
        allowed = {
            "nombre":         t_rep.c.NOMBRE,
            "disponible":     t_rep.c.DISPONIBLE,
            "amonestaciones": t_rep.c.AMONESTACIONES,
            "rating":         t_rep.c.RATING,
        }
        values = {allowed[k].key: v for k, v in data.items() if k in allowed}
        tiene_estado = "estado" in data  
        if not values and not tiene_estado:
            return False                  # solo retorna si no hay NADA que hacer
        with engine.begin() as conn:
            affected = 0
            if values:
                result = conn.execute(
                    update(t_rep).where(t_rep.c.ID == id_rep).values(**values)
                )
                affected = result.rowcount
                # Suspender cuenta si acumula 4 amonestaciones
                if "amonestaciones" in data and data["amonestaciones"] >= 4:
                    row = conn.execute(
                        select(t_rep.c.USUARIO_ID).where(t_rep.c.ID == id_rep)
                    ).first()
                    if row:
                        conn.execute(
                            update(t_usuario).where(t_usuario.c.ID == row[0]).values(ESTADO=0)
                        )
            if tiene_estado:
                row = conn.execute(
                    select(t_rep.c.USUARIO_ID).where(t_rep.c.ID == id_rep)
                ).first()
                if row:
                    conn.execute(
                        update(t_usuario).where(t_usuario.c.ID == row[0])
                        .values(ESTADO=data["estado"])
                    )
                    affected = 1   # confirma que algo se hizo
            return affected > 0
    

    @staticmethod
    def _map(row) -> Repartidor:
        return Repartidor(
            id=row["ID"],
            usuario_id=row["USUARIO_ID"],
            cedula=row["CEDULA"],
            nombre=row["NOMBRE"],
            correo=row["CORREO"],
            direccion=row["DIRECCION"],
            telefono=row["TELEFONO"],
            tarjeta=row["TARJETA"],
            disponible=row["DISPONIBLE"],
            km_recorridos_diarios=row["KM_RECORRIDOS_DIARIOS"],
            costo_km_habil=row["COSTO_KM_HABIL"],
            costo_km_feriado=row["COSTO_KM_FERIADO"],
            amonestaciones=row["AMONESTACIONES"]
        )
    
