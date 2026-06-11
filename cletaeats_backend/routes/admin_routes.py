# routes/admin_routes.py
from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from middleware.auth_middleware import require_admin
from data.repositories.cliente_repository import ClienteRepository
from data.repositories.repartidor_repository import RepartidorRepository
from data.repositories.restaurante_repository import RestauranteRepository
from data.repositories.combo_repository import ComboRepository
from data.repositories.categoria_repository import CategoriaRepository
from data.repositories.producto_repository import ProductoRepository
from data.repositories.pedido_repository import PedidoRepository
from data.repositories.usuario_repository import UsuarioRepository
from fastapi import File, UploadFile
import uuid, os, shutil


router = APIRouter()

# ── Repos (instancias únicas por request gracias a FastAPI DI) ────────
def get_repos():
    return {
        "cliente":     ClienteRepository(),
        "repartidor":  RepartidorRepository(),
        "restaurante": RestauranteRepository(),
        "combo":       ComboRepository(),
        "categoria":   CategoriaRepository(),
        "producto":    ProductoRepository(),
        "pedido":      PedidoRepository(),
        "usuario":     UsuarioRepository(),
    }

# ── Pydantic models ───────────────────────────────────────────────────
class RestauranteBody(BaseModel):
    cedula_juridica: str
    nombre: str
    direccion: str
    imagen_url: str = ""
    estado: Optional[int] = None
    categoria_ids: Optional[List[int]] = None
    latitud: Optional[float] = None
    longitud: Optional[float] = None 

class ComboBody(BaseModel):
    restaurante_id: Optional[int] = None
    numero_combo: Optional[int] = None
    nombre: Optional[str] = None
    descripcion: str = ""
    precio: Optional[float] = None
    estado: Optional[int] = None
    producto_ids: Optional[List[int]] = None

class RepartidorBody(BaseModel):
    nombre:        Optional[str] = None
    disponible:    Optional[int] = None   
    amonestaciones:Optional[int] = None
    estado: Optional[int] = None  # nuevo: 1=activo, 0=bloqueado login

class ClienteEstadoBody(BaseModel):
    estado: int

class CategoriaBody(BaseModel):
    nombre: str

class ProductoBody(BaseModel):
    restaurante_id: Optional[int] = None
    nombre: Optional[str] = None
    descripcion: str = ""
    estado: Optional[int] = None

class AmonestacionBody(BaseModel):
    motivo: str = ""

# ── Clientes ──────────────────────────────────────────────────────────
@router.get("/clientes")
def listar_clientes(_=Depends(require_admin), repos=Depends(get_repos)):
    return repos["cliente"].listar_todos()

@router.put("/clientes/{id}")
def actualizar_cliente(id: int, body: ClienteEstadoBody, _=Depends(require_admin), repos=Depends(get_repos)):
    ok = repos["cliente"].actualizar_estado_usuario(id, body.estado)
    if not ok:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    return {"mensaje": "Estado actualizado"}

# ── Repartidores ──────────────────────────────────────────────────────
@router.get("/repartidores")
def listar_repartidores(_=Depends(require_admin), repos=Depends(get_repos)):
    return repos["repartidor"].listar_todos()

@router.put("/repartidores/{id}")
def actualizar_repartidor(id: int, body: RepartidorBody, _=Depends(require_admin), repos=Depends(get_repos)):
    data = body.model_dump(exclude_none=True)
    ok = repos["repartidor"].actualizar_campos(id, data)
    if not ok:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    return {"mensaje": "Repartidor actualizado"}

# ── Restaurantes ──────────────────────────────────────────────────────
@router.get("/restaurantes")
def listar_restaurantes(_=Depends(require_admin), repos=Depends(get_repos)):
    return repos["restaurante"].listar_todos()

@router.post("/restaurantes", status_code=201)
def crear_restaurante(body: RestauranteBody, _=Depends(require_admin), repos=Depends(get_repos)):
    if repos["restaurante"].buscar_por_cedula(body.cedula_juridica):
        raise HTTPException(status_code=409, detail="Ya existe un restaurante con esa cédula jurídica")
    nuevo_id = repos["restaurante"].crear(body.model_dump(exclude_none=True))
    if body.categoria_ids:
        repos["categoria"].asignar_a_restaurante(nuevo_id, body.categoria_ids)
    return {"id": nuevo_id, "mensaje": "Restaurante creado"}

@router.put("/restaurantes/{id}")
def actualizar_restaurante(id: int, body: RestauranteBody, _=Depends(require_admin), repos=Depends(get_repos)):
    data = body.model_dump(exclude_none=True)
    cat_ids = data.pop("categoria_ids", None)
    repos["restaurante"].actualizar_campos(id, data)
    if cat_ids is not None:
        repos["categoria"].asignar_a_restaurante(id, cat_ids)
    return {"mensaje": "Restaurante actualizado"}

# ── Categorías ────────────────────────────────────────────────────────
@router.get("/categorias")
def listar_categorias(_=Depends(require_admin), repos=Depends(get_repos)):
    return repos["categoria"].listar_todas()

@router.post("/categorias", status_code=201)
def crear_categoria(body: CategoriaBody, _=Depends(require_admin), repos=Depends(get_repos)):
    nuevo_id = repos["categoria"].crear(body.nombre)
    return {"id": nuevo_id, "mensaje": "Categoría creada"}

@router.delete("/categorias/{id}")
def eliminar_categoria(id: int, _=Depends(require_admin), repos=Depends(get_repos)):
    ok = repos["categoria"].eliminar(id)
    if not ok:
        raise HTTPException(status_code=404, detail="Categoría no encontrada")
    return {"mensaje": "Categoría eliminada"}

# ── Productos ─────────────────────────────────────────────────────────
@router.get("/productos")
def listar_productos(_=Depends(require_admin), repos=Depends(get_repos)):
    return repos["producto"].listar_todos()

@router.post("/productos", status_code=201)
def crear_producto(body: ProductoBody, _=Depends(require_admin), repos=Depends(get_repos)):
    if not body.nombre or not body.restaurante_id:
        raise HTTPException(status_code=400, detail="nombre y restaurante_id son requeridos")
    nuevo_id = repos["producto"].crear(body.model_dump(exclude_none=True))
    return {"id": nuevo_id, "mensaje": "Producto creado"}

@router.put("/productos/{id}")
def actualizar_producto(id: int, body: ProductoBody, _=Depends(require_admin), repos=Depends(get_repos)):
    data = body.model_dump(exclude_none=True)
    ok = repos["producto"].actualizar_campos(id, data)
    if not ok:
        raise HTTPException(status_code=404, detail="Producto no encontrado")
    return {"mensaje": "Producto actualizado"}

# ── Combos ────────────────────────────────────────────────────────────
@router.get("/combos")
def listar_combos(_=Depends(require_admin), repos=Depends(get_repos)):
    return repos["combo"].listar_todos()

@router.post("/combos", status_code=201)
def crear_combo(body: ComboBody, _=Depends(require_admin), repos=Depends(get_repos)):
    if not body.nombre or not body.precio:
        raise HTTPException(status_code=400, detail="nombre y precio son requeridos")
    nuevo_id = repos["combo"].crear(body.model_dump(exclude_none=True))
    return {"id": nuevo_id, "mensaje": "Combo creado"}

@router.put("/combos/{id}")
def actualizar_combo(id: int, body: ComboBody, _=Depends(require_admin), repos=Depends(get_repos)):
    ok = repos["combo"].actualizar_campos(id, body.model_dump(exclude_none=True))
    if not ok:
        raise HTTPException(status_code=404, detail="Combo no encontrado")
    return {"mensaje": "Combo actualizado"}

# ── Pedidos ───────────────────────────────────────────────────────────
@router.get("/pedidos")
def listar_pedidos(_=Depends(require_admin), repos=Depends(get_repos)):
    return repos["pedido"].listar_todos()

# ── Upload de imagen ──────────────────────────────────────────────────
@router.post("/upload-imagen")
async def upload_imagen(file: UploadFile = File(...), _=Depends(require_admin)):
    os.makedirs("uploads", exist_ok=True)
    extension = file.filename.split(".")[-1]
    nombre = f"{uuid.uuid4()}.{extension}"
    ruta = f"uploads/{nombre}"
    with open(ruta, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    return {"url": f"/uploads/{nombre}"}

# Amonestaciones a repartidores ─────────────────────────────────────────
@router.post("/repartidores/{id}/amonestacion")
def agregar_amonestacion(id: int, body: AmonestacionBody, _=Depends(require_admin), repos=Depends(get_repos)):
    rep = repos["repartidor"].obtener_por_id(id)
    if not rep:
        raise HTTPException(status_code=404, detail="Repartidor no encontrado")
    if rep.amonestaciones >= 4:
        raise HTTPException(status_code=400, detail="El repartidor ya está suspendido")

    nuevas = rep.amonestaciones + 1
    data = {"amonestaciones": nuevas}
    if nuevas >= 4:
        data["estado"] = 0  # suspensión automática, también actualiza USUARIO via actualizar_campos

    repos["repartidor"].actualizar_campos(id, data)
    return {
        "amonestaciones": nuevas,
        "suspendido": nuevas >= 4,
        "mensaje": "Repartidor suspendido" if nuevas >= 4 else "Amonestación registrada"
    }

# ── Reportes ───────────────────────────────────────────────────────────
@router.get("/reportes")
def obtener_reportes(_=Depends(require_admin), repos=Depends(get_repos)):
    return {
        "restaurante_mas_pedidos":   repos["pedido"].restaurante_mas_pedidos(),
        "restaurante_menos_pedidos": repos["pedido"].restaurante_menos_pedidos(),
        "monto_por_restaurante":     repos["pedido"].monto_por_restaurante(),
        "cliente_top":               repos["pedido"].cliente_top(),
        "hora_pico":                 repos["pedido"].hora_pico(),
    }