# routes/cliente_routes.py
from fastapi import APIRouter, Depends, File, HTTPException, UploadFile
from pydantic import BaseModel
from middleware.auth_middleware import get_current_user
from data.repositories.tarjeta_cliente_repository import TarjetaClienteRepository
from data.repositories.cliente_repository import ClienteRepository
from data.repositories.bitacora_repository import BitacoraRepository

from typing import List, Optional
from data.repositories.pedido_repository import PedidoRepository
from data.repositories.combo_repository import ComboRepository
from data.repositories.restaurante_repository import RestauranteRepository
from data.repositories.repartidor_repository import RepartidorRepository
from data.repositories.queja_repository import QuejaRepository
from core.entities.queja import Queja
from core.use_cases.pedido_use_cases import PedidoUseCases
from data.database.tables import usuario as t_usuario
from sqlalchemy import select
from data.database.db_connection import engine

router = APIRouter()

class TarjetaBody(BaseModel):
    numero: str
    alias: str = ""
    fecha_vencimiento: str = ""
    cvv: str = ""
    es_principal: int = 0

class ItemPedido(BaseModel):
    combo_id: int
    cantidad: int = 1
    configuracion: str = "{}"   # JSON con extras elegidos y acompañamiento

class PedidoBody(BaseModel):
    restaurante_id: int
    distancia_km: float = 5.0
    items: List[ItemPedido]

class PerfilBody(BaseModel):
    nombre:    str
    telefono:  str
    direccion: str

class ImagenPerfilBody(BaseModel):
    imagen_url: str

class RatingBody(BaseModel):
    rating: int  # 1 a 5

class QuejaBody(BaseModel):
    pedido_id: int
    motivo: str           # amabilidad, tiempo, presentacion, otro
    descripcion: str = ""

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
        "cliente_id":        cliente.id,
        "numero":            body.numero,
        "alias":             body.alias,
        "fecha_vencimiento": body.fecha_vencimiento,
        "cvv":               body.cvv,
        "es_principal":      body.es_principal
    })
    BitacoraRepository().registrar(
        usuario_id = sesion["id_usuario"],
        rol        = "CLIENTE",
        accion     = "TARJETA_AGREGADA",
        detalle    = f"Alias: {body.alias}"
    )
    return {"id": nuevo_id, "mensaje": "Tarjeta agregada"}

@router.put("/tarjetas/{id}")
def actualizar_tarjeta(id: int, body: TarjetaBody, sesion: dict = Depends(get_current_user)):
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    repo = TarjetaClienteRepository()
    tarjeta = repo.obtener_por_id(id)  # necesitas este método si no existe
    if not tarjeta or tarjeta["CLIENTE_ID"] != cliente.id:
        raise HTTPException(status_code=403, detail="Tarjeta no pertenece al cliente")
    repo.actualizar({
        "id":               id,
        "alias":            body.alias,
        "fecha_vencimiento": body.fecha_vencimiento,
        "cvv":              body.cvv,
        "es_principal":     body.es_principal
    })
    return {"mensaje": "Tarjeta actualizada"}

@router.delete("/tarjetas/{id}")
def eliminar_tarjeta(id: int, sesion: dict = Depends(get_current_user)):
    ok = TarjetaClienteRepository().eliminar(id)
    if not ok:
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
    BitacoraRepository().registrar(
        usuario_id = sesion["id_usuario"],
        rol        = "CLIENTE",
        accion     = "TARJETA_ELIMINADA",
        detalle    = f"Tarjeta ID: {id}"
    )
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

    with engine.connect() as conn:
        row = conn.execute(
            select(t_usuario.c.ESTADO).where(t_usuario.c.ID == sesion["id_usuario"])
        ).scalar()
    if row == 0:
        raise HTTPException(status_code=403, detail="Tu cuenta está suspendida")

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
    BitacoraRepository().registrar(
        usuario_id = sesion["id_usuario"],
        rol        = "CLIENTE",
        accion     = "PEDIDO_CREADO",
        detalle    = f"Restaurante ID: {body.restaurante_id}, Items: {len(body.items)}"
    )
    return {"mensaje": "Pedido creado", "factura": factura}

@router.get("/pedidos/{id_pedido}/factura")
def ver_factura(id_pedido: int, sesion: dict = Depends(get_current_user)):
    factura = PedidoRepository().obtener_factura(id_pedido)
    if not factura:
        raise HTTPException(status_code=404, detail="Pedido no encontrado")
    return factura

@router.get("/perfil")
def obtener_perfil(sesion: dict = Depends(get_current_user)):
    """Devuelve los datos del perfil del cliente autenticado."""
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    
    
    return {
        "id":         cliente.id,
        "nombre":     cliente.nombre,
        "telefono":   cliente.telefono,
        "direccion":  cliente.direccion,
        "cedula":     cliente.cedula,     # solo lectura en la respuesta
        "imagen_url": cliente.imagen_url or ""
    }


@router.put("/perfil")
def actualizar_perfil(body: PerfilBody, sesion: dict = Depends(get_current_user)):
    """Actualiza nombre, teléfono y dirección del cliente autenticado."""
    from sqlalchemy import update
    from data.database.db_connection import engine
    from data.database.tables import cliente as t_cliente

    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")

    with engine.begin() as conn:
        conn.execute(
            update(t_cliente)
            .where(t_cliente.c.ID == cliente.id)
            .values(
                NOMBRE    = body.nombre.strip(),
                TELEFONO  = body.telefono.strip(),
                DIRECCION = body.direccion.strip()
            )
        )
    BitacoraRepository().registrar(
        usuario_id = sesion["id_usuario"],
        rol        = "CLIENTE",
        accion     = "PERFIL_EDITADO",
        detalle    = f"Nombre: {body.nombre}"
    )
    return {"mensaje": "Perfil actualizado correctamente"}

@router.put("/perfil/imagen")
def actualizar_imagen_perfil(body: ImagenPerfilBody, sesion: dict = Depends(get_current_user)):
    repo = ClienteRepository()
    cliente = repo.encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    repo.actualizar_imagen_url(cliente.id, body.imagen_url)
    BitacoraRepository().registrar(
        usuario_id = sesion["id_usuario"],
        rol        = "CLIENTE",
        accion     = "PERFIL_EDITADO",
        detalle    = f"Imagen actualizada"
    )
    return {"mensaje": "Imagen actualizada"}

@router.post("/upload-imagen")
def upload_imagen_cliente(file: UploadFile = File(...), sesion: dict = Depends(get_current_user)):
    # Mismo código que admin/upload-imagen
    import uuid, os
    from fastapi import UploadFile, File
    ext = file.filename.rsplit(".", 1)[-1] if "." in file.filename else "jpg"
    nombre = f"{uuid.uuid4().hex}.{ext}"
    ruta = os.path.join("uploads", nombre)
    os.makedirs("uploads", exist_ok=True)
    with open(ruta, "wb") as f:
        f.write(file.file.read())
    return {"url": f"/uploads/{nombre}"}

@router.post("/pedidos/{id_pedido}/rating")
def calificar_repartidor(id_pedido: int, body: RatingBody, sesion: dict = Depends(get_current_user)):
    if body.rating < 1 or body.rating > 5:
        raise HTTPException(status_code=400, detail="El rating debe ser entre 1 y 5")
    pedido = PedidoRepository().obtener_por_id(id_pedido)
    if not pedido:
        raise HTTPException(status_code=404, detail="Pedido no encontrado")
    if pedido.estado != 3:
        raise HTTPException(status_code=400, detail="Solo se puede calificar un pedido entregado")

    # Promediar el rating
    rep = RepartidorRepository().obtener_por_id(pedido.repartidor_id)
    if rep:
        nuevo_rating = round((rep.rating + body.rating) / 2, 2) if rep.rating else float(body.rating)
        RepartidorRepository().actualizar_campos(pedido.repartidor_id, {"rating": nuevo_rating})

    # Marcar el pedido como ya calificado
    from sqlalchemy import update
    from data.database.db_connection import engine
    from data.database.tables import pedido as t_pedido
    with engine.begin() as conn:
        conn.execute(
            update(t_pedido)
            .where(t_pedido.c.ID == id_pedido)
            .values(CALIFICADO=1, RATING_DADO=body.rating)
        )
    return {"mensaje": "Calificación registrada"}

@router.put("/pedidos/{id_pedido}/cancelar")
def cancelar_pedido(id_pedido: int, sesion: dict = Depends(get_current_user)):
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    pedido = PedidoRepository().obtener_por_id(id_pedido)
    if not pedido:
        raise HTTPException(status_code=404, detail="Pedido no encontrado")
    if pedido.cliente_id != cliente.id:
        raise HTTPException(status_code=403, detail="Este pedido no te pertenece")
    if pedido.estado != 0:
        raise HTTPException(status_code=400, detail="Solo se puede cancelar un pedido recién creado")
    ok = PedidoRepository().actualizar_estado(id_pedido, 4)
    if not ok:
        raise HTTPException(status_code=400, detail="No se pudo cancelar el pedido")
    # Liberar repartidor si estaba asignado (vuelve a disponible)
    if pedido.repartidor_id:
        RepartidorRepository().actualizar_disponibilidad(pedido.repartidor_id, 1)
    BitacoraRepository().registrar(
        usuario_id = sesion["id_usuario"],
        rol        = "CLIENTE",
        accion     = "PEDIDO_CANCELADO",
        detalle    = f"Pedido ID: {id_pedido}"
    )
    return {"mensaje": "Pedido cancelado"}

@router.post("/quejas")
def crear_queja(body: QuejaBody, sesion: dict = Depends(get_current_user)):
    """
    El cliente registra una queja sobre un pedido propio que tenga
    repartidor asignado. La queja entra en estado PENDIENTE para que
    el admin la revise.
    """
    cliente = ClienteRepository().encontrar_por_usuario_id(sesion["id_usuario"])
    if not cliente:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")

    pedido = PedidoRepository().obtener_por_id(body.pedido_id)
    if not pedido:
        raise HTTPException(status_code=404, detail="Pedido no encontrado")
    if pedido.cliente_id != cliente.id:
        raise HTTPException(status_code=403, detail="Este pedido no te pertenece")
    if not pedido.repartidor_id:
        raise HTTPException(status_code=400, detail="El pedido no tiene repartidor asignado")

    motivos_validos = {"amabilidad", "tiempo", "presentacion", "otro"}
    if body.motivo not in motivos_validos:
        raise HTTPException(status_code=400, detail="Motivo inválido")

    nueva = Queja(
        cliente_id=cliente.id,
        repartidor_id=pedido.repartidor_id,
        pedido_id=pedido.id,
        motivo=body.motivo,
        descripcion=body.descripcion,
    )
    creada = QuejaRepository().crear(nueva)
    return {"mensaje": "Queja registrada", "queja_id": creada.id}
