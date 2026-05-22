# routes/cliente_routes.py
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from middleware.auth_middleware import get_current_user
from data.repositories.tarjeta_cliente_repository import TarjetaClienteRepository
from data.repositories.cliente_repository import ClienteRepository

from typing import List, Optional
from data.repositories.pedido_repository import PedidoRepository
from data.repositories.combo_repository import ComboRepository
from data.repositories.restaurante_repository import RestauranteRepository
from data.repositories.repartidor_repository import RepartidorRepository
from core.use_cases.pedido_use_cases import PedidoUseCases

router = APIRouter()

class TarjetaBody(BaseModel):
    numero: str
    alias: str = ""
    es_principal: int = 0

class ItemPedido(BaseModel):
    combo_id: int
    cantidad: int = 1
    configuracion: str = "{}"   # JSON con extras elegidos y acompañamiento

class PedidoBody(BaseModel):
    restaurante_id: int
    distancia_km: float = 5.0
    items: List[ItemPedido]

@router.get("/tarjetas")
def listar_tarjetas(sesion: dict = Depends(get_current_user)):
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    return TarjetaClienteRepository().listar_por_cliente(cliente.id)

@router.post("/tarjetas", status_code=201)
def agregar_tarjeta(body: TarjetaBody, sesion: dict = Depends(get_current_user)):
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    nuevo_id = TarjetaClienteRepository().crear({
        "cliente_id":   cliente.id,
        "numero":       body.numero,
        "alias":        body.alias,
        "es_principal": body.es_principal
    })
    return {"id": nuevo_id, "mensaje": "Tarjeta agregada"}

@router.delete("/tarjetas/{id}")
def eliminar_tarjeta(id: int, sesion: dict = Depends(get_current_user)):
    ok = TarjetaClienteRepository().eliminar(id)
    if not ok:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    return {"mensaje": "Tarjeta eliminada"}


@router.get("/pedidos")
def mis_pedidos(sesion: dict = Depends(get_current_user)):
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    return PedidoRepository().listar_por_cliente(cliente.id)

@router.post("/pedidos", status_code=201)
def crear_pedido(body: PedidoBody, sesion: dict = Depends(get_current_user)):
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")

    use_cases = PedidoUseCases(
        pedido_repo=PedidoRepository(),
        combo_repo=ComboRepository(),
        restaurante_repo=RestauranteRepository(),
        repartidor_repo=RepartidorRepository(),
    )
    items_input = [
        {"combo_id": i.combo_id, "cantidad": i.cantidad, "configuracion": i.configuracion}
        for i in body.items
    ]
    ok, factura, error = use_cases.crear_pedido(
        id_cliente=cliente.id,
        id_restaurante=body.restaurante_id,
        items_input=items_input,
        distancia_km=body.distancia_km
    )
    if not ok:
        raise HTTPException(status_code=400, detail=error)
    return {"mensaje": "Pedido creado", "factura": factura}

@router.get("/pedidos/{id_pedido}/factura")
def ver_factura(id_pedido: int, sesion: dict = Depends(get_current_user)):
    factura = PedidoRepository().obtener_factura(id_pedido)
    if not factura:
        raise HTTPException(status_code=404, detail="Pedido no encontrado")
    return factura