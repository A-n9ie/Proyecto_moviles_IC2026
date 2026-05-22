# routes/repartidor_routes.py
from fastapi import APIRouter, Depends, HTTPException
from middleware.auth_middleware import get_current_user
from data.repositories.pedido_repository import PedidoRepository
from data.repositories.repartidor_repository import RepartidorRepository

router = APIRouter()

@router.get("/pedidos")
def mis_pedidos_repartidor(sesion: dict = Depends(get_current_user)):
    """Pedidos asignados al repartidor logueado (estados 0,1,2)"""
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    return PedidoRepository().listar_por_repartidor(rep.id)

@router.put("/pedidos/{id_pedido}/entregar")
def marcar_entregado(id_pedido: int, sesion: dict = Depends(get_current_user)):
    rep = RepartidorRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    pedido = PedidoRepository().obtener_por_id(id_pedido)
    if not pedido:
        raise HTTPException(status_code=404, detail="Pedido no encontrado")
    if pedido.repartidor_id != rep.id:
        raise HTTPException(status_code=403, detail="Este pedido no te pertenece")
    ok = PedidoRepository().actualizar_estado(id_pedido, 3)
    if not ok:
        raise HTTPException(status_code=400, detail="No se pudo actualizar")
    RepartidorRepository().actualizar_estado(rep.id, 1)
    return {"mensaje": "Pedido marcado como entregado"}