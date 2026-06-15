# routes/auth_routes.py
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

from core.use_cases.auth_use_cases import AuthUseCases
from data.repositories.usuario_repository import UsuarioRepository
from data.repositories.cliente_repository import ClienteRepository
from data.repositories.repartidor_repository import RepartidorRepository
from data.repositories.tarjeta_cliente_repository import TarjetaClienteRepository
from services.jwt_service import crear_token
from data.repositories.bitacora_repository import BitacoraRepository

router = APIRouter()

_auth_uc = AuthUseCases(
    UsuarioRepository(),
    ClienteRepository(),
    RepartidorRepository(),
    TarjetaClienteRepository(),
)

ROLES_POR_PLATAFORMA = {
    "WEB":    {"ADMIN", "EMPLEADO"},
    "MOBILE": {"CLIENTE", "REPARTIDOR"},
}


class LoginRequest(BaseModel):
    email:    str
    password: str
    platform: str = "MOBILE"

class RegistroClienteRequest(BaseModel):
    email:              str
    password:           str
    confirmar_password: str
    cedula:             str
    nombre:             str
    direccion:          str
    telefono:           str
    numero_tarjeta:     str  = ""
    fecha_vencimiento:  str  = ""
    cvv_tarjeta:        str  = ""

class RegistroRepartidorRequest(BaseModel):
    email:              str
    password:           str
    confirmar_password: str
    cedula:             str
    nombre:             str
    correo_contacto:    str = ""
    direccion:          str
    telefono:           str
    tarjeta:            str = "0000000000000000"


@router.post("/login")
def login(body: LoginRequest):
    ok, datos, error = _auth_uc.login(body.email, body.password)
    if not ok:
        raise HTTPException(status_code=401, detail=error)

    roles_ok = ROLES_POR_PLATAFORMA.get(body.platform.upper(), set())
    if datos["rol"] not in roles_ok:
        raise HTTPException(
            status_code=403,
            detail=f"Rol '{datos['rol']}' no permitido en plataforma '{body.platform}'"
        )

    token = crear_token(**datos)
    BitacoraRepository().registrar(
        usuario_id = datos.get("id_usuario") or datos.get("id_perfil"),
        rol        = datos.get("rol"),
        accion     = "LOGIN",
        detalle    = f"Inicio de sesión exitoso"
    )
    return {"token": token, **datos}


@router.post("/registro/cliente", status_code=201)
def registro_cliente(body: RegistroClienteRequest):
    ok, datos, error = _auth_uc.registro_cliente(   # sin tarjeta=""
        email=body.email,
        password=body.password,
        confirmar_password=body.confirmar_password,
        cedula=body.cedula,
        nombre=body.nombre,
        direccion=body.direccion,
        telefono=body.telefono,
    )
    if not ok:                          # verificar error PRIMERO
        raise HTTPException(status_code=400, detail=error)

    if body.numero_tarjeta:             # crear tarjeta solo si registro ok
        TarjetaClienteRepository().crear({
            "cliente_id":        datos["id_perfil"],
            "numero":            body.numero_tarjeta,
            "alias":             "Tarjeta principal",
            "fecha_vencimiento": body.fecha_vencimiento,
            "cvv":               body.cvv_tarjeta,
            "es_principal":      1
        })

    token = crear_token(**datos)
    return {"token": token, **datos}


@router.post("/registro/repartidor", status_code=201)
def registro_repartidor(body: RegistroRepartidorRequest):
    ok, datos, error = _auth_uc.registro_repartidor(
        email=body.email,
        password=body.password,
        confirmar_password=body.confirmar_password,
        cedula=body.cedula,
        nombre=body.nombre,
        correo_contacto=body.correo_contacto or body.email,
        direccion=body.direccion,
        telefono=body.telefono,
        tarjeta=body.tarjeta,
    )
    if not ok:
        raise HTTPException(status_code=400, detail=error)

    token = crear_token(**datos)
    return {"token": token, **datos}


@router.post("/logout")
def logout():
    return {"mensaje": "Sesión cerrada. Elimina el token en el cliente."}