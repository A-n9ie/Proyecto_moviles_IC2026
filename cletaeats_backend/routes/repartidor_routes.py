# routes/repartidor_routes.py
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException
from middleware.auth_middleware import get_current_user
from data.repositories.pedido_repository import PedidoRepository
from data.repositories.repartidor_repository import RepartidorRepository
from pydantic import BaseModel
from data.repositories.bitacora_repository import BitacoraRepository

router = APIRouter()

class TarjetaRepartidorBody(BaseModel):
    tarjeta: str   # número de tarjeta de pago del repartidor

class PerfilRepartidorBody(BaseModel):
    nombre:    str
    telefono:  str
    direccion: str

def _validar_pedido_del_repartidor(id_pedido: int, sesion: dict):
    """Helper: valida que el repartidor exista y que el pedido le pertenezca.
    Devuelve (repartidor, pedido) o lanza HTTPException."""
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    pedido = PedidoRepository().obtener_por_id(id_pedido)
    if not pedido:
        raise HTTPException(status_code=404, detail="Pedido no encontrado")
    if pedido.repartidor_id != rep.id:
        raise HTTPException(status_code=403, detail="Este pedido no te pertenece")
    return rep, pedido


@router.get("/pedidos")
def mis_pedidos_repartidor(sesion: dict = Depends(get_current_user)):
    """Pedidos asignados al repartidor logueado (estados 0,1,2)"""
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    return PedidoRepository().listar_por_repartidor(rep.id)

@router.get("/historial")
def historial_entregas(sesion: dict = Depends(get_current_user)):
    """Historial de pedidos ENTREGADOS del repartidor logueado."""
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    return PedidoRepository().listar_entregados_por_repartidor(rep.id)

@router.put("/pedidos/{id_pedido}/preparar")
def marcar_preparando(id_pedido: int, sesion: dict = Depends(get_current_user)):
    """Transición CREADO (0) -> EN_PREPARACION (1). El repartidor acepta el pedido."""
    rep, pedido = _validar_pedido_del_repartidor(id_pedido, sesion)
    if pedido.estado != 0:
        raise HTTPException(
            status_code=400,
            detail="Solo se puede aceptar un pedido en estado CREADO"
        )
    ok = PedidoRepository().actualizar_estado(id_pedido, 1)
    if not ok:
        raise HTTPException(status_code=400, detail="No se pudo actualizar")
    return {"mensaje": "Pedido en preparación", "estado": 1}


@router.put("/pedidos/{id_pedido}/en-camino")
def marcar_en_camino(id_pedido: int, sesion: dict = Depends(get_current_user)):
    """Transición EN_PREPARACION (1) -> EN_CAMINO (2). El repartidor sale del restaurante."""
    rep, pedido = _validar_pedido_del_repartidor(id_pedido, sesion)
    if pedido.estado != 1:
        raise HTTPException(
            status_code=400,
            detail="El pedido debe estar EN_PREPARACION para salir a entregar"
        )
    ok = PedidoRepository().actualizar_estado(id_pedido, 2)
    if not ok:
        raise HTTPException(status_code=400, detail="No se pudo actualizar")
    return {"mensaje": "Pedido en camino", "estado": 2}


@router.put("/pedidos/{id_pedido}/entregar")
def marcar_entregado(id_pedido: int, sesion: dict = Depends(get_current_user)):
    """Transición EN_CAMINO (2) -> ENTREGADO (3). El repartidor entrega y queda disponible."""
    rep, pedido = _validar_pedido_del_repartidor(id_pedido, sesion)
    if pedido.estado != 2:
        raise HTTPException(
            status_code=400,
            detail="El pedido debe estar EN_CAMINO para marcarse como entregado"
        )
    fecha_entrega = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    ok = PedidoRepository().actualizar_estado(id_pedido, 3, fecha_entrega)
    if not ok:
        raise HTTPException(status_code=400, detail="No se pudo actualizar")
    RepartidorRepository().actualizar_disponibilidad(rep.id, 1)  # repartidor vuelve a disponible
    BitacoraRepository().registrar(
        usuario_id = sesion["id_usuario"],
        rol        = "REPARTIDOR",
        accion     = "PEDIDO_ENTREGADO",
        detalle    = f"Pedido ID: {id_pedido}"
    )
    return {"mensaje": "Pedido marcado como entregado", "estado": 3}

@router.get("/perfil")
def obtener_perfil_repartidor(sesion: dict = Depends(get_current_user)):
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    return {
        "id":        rep.id,
        "nombre":    rep.nombre,
        "telefono":  rep.telefono,
        "direccion": rep.direccion,
        "correo":    rep.correo,
        "cedula":    rep.cedula,
        "tarjeta":   rep.tarjeta,
        "rating":    rep.rating,
        "km_recorridos_diarios": rep.km_recorridos_diarios,
        "amonestaciones": rep.amonestaciones,
    }

@router.put("/perfil")
def actualizar_perfil_repartidor(
    body: PerfilRepartidorBody,
    sesion: dict = Depends(get_current_user)
):
    from sqlalchemy import update
    from data.database.db_connection import engine
    from data.database.tables import repartidor as t_rep
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    with engine.begin() as conn:
        conn.execute(
            update(t_rep).where(t_rep.c.ID == rep.id).values(
                NOMBRE    = body.nombre.strip(),
                TELEFONO  = body.telefono.strip(),
                DIRECCION = body.direccion.strip()
            )
        )
    return {"mensaje": "Perfil actualizado"}

@router.put("/perfil/tarjeta")
def actualizar_tarjeta_repartidor(
    body: TarjetaRepartidorBody,
    sesion: dict = Depends(get_current_user)
):
    from sqlalchemy import update
    from data.database.db_connection import engine
    from data.database.tables import repartidor as t_rep
    if not body.tarjeta.strip():
        raise HTTPException(status_code=400, detail="La tarjeta no puede estar vacía")
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    with engine.begin() as conn:
        conn.execute(
            update(t_rep).where(t_rep.c.ID == rep.id)
                         .values(TARJETA=body.tarjeta.strip())
        )
    return {"mensaje": "Tarjeta actualizada"}