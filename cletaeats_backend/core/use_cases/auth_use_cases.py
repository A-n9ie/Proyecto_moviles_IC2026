# core/use_cases/auth_use_cases.py
from typing import Tuple, Optional
from domain.interfaces.i_usuario_repository import IUsuarioRepository
from domain.interfaces.i_cliente_repository import IClienteRepository
from domain.interfaces.i_repartidor_repository import IRepartidorRepository
from domain.interfaces.i_tarjeta_cliente_repository import ITarjetaClienteRepository
from core.entities.usuario import Usuario
from core.entities.cliente import Cliente
from core.entities.repartidor import Repartidor
from core.entities.tarjeta_cliente import TarjetaCliente
from services.hash_service import hash_password, verify_password

_ROLES_MOVIL = {"CLIENTE", "REPARTIDOR"}

class AuthUseCases:
    def __init__(
        self,
        usuario_repo: IUsuarioRepository,
        cliente_repo: IClienteRepository,
        repartidor_repo: IRepartidorRepository,
        tarjeta_repo: ITarjetaClienteRepository
    ):
        self._usuarios      = usuario_repo
        self._clientes      = cliente_repo
        self._repartidores  = repartidor_repo
        self._tarjetas      = tarjeta_repo

    # ─── LOGIN ──────────────────────────────────────────────────────
    def login(self, email: str, password: str) -> Tuple[bool, Optional[dict], Optional[str]]:
        email = (email or "").strip().lower()
        if not email or not password:
            return False, None, "Email y contraseña son requeridos"

        usuario = self._usuarios.encontrar_por_email(email)
        if usuario is None:
            return False, None, "Usuario no encontrado"

        if not usuario.es_activo():
            return False, None, "Cuenta suspendida. Contacte al administrador"

        if not verify_password(password, usuario.password_hash):
            return False, None, "Contraseña incorrecta"

        nombre, id_perfil = self._obtener_perfil(usuario)
        datos = {
            "id_usuario": usuario.id,
            "email":      usuario.email,
            "rol":        usuario.rol,
            "nombre":     nombre,
            "id_perfil":  id_perfil
        }
        return True, datos, None

    # ─── REGISTRO CLIENTE ───────────────────────────────────────────
    def registro_cliente(
        self,
        email: str,
        password: str,
        confirmar_password: str,
        cedula: str,
        nombre: str,
        direccion: str,
        telefono: str,
    ) -> Tuple[bool, Optional[dict], Optional[str]]:

        email    = (email or "").strip().lower()
        password = (password or "").strip()
        cedula   = (cedula or "").strip()
        nombre   = (nombre or "").strip()

        if not all([email, password, confirmar_password, cedula, nombre, direccion, telefono]):
            return False, None, "Todos los campos son requeridos"
        if password != (confirmar_password or "").strip():
            return False, None, "Las contraseñas no coinciden"
        if len(password) < 6:
            return False, None, "La contraseña debe tener al menos 6 caracteres"
        if self._usuarios.existe_email(email):
            return False, None, "Ese correo ya está registrado"
        if self._clientes.existe_cedula(cedula):
            return False, None, "Esa cédula ya está registrada"

        usuario = self._usuarios.crear(Usuario(
            email=email,
            password_hash=hash_password(password),
            rol="CLIENTE",
            estado=1
        ))

        cliente = self._clientes.crear(Cliente(
            usuario_id=usuario.id,
            cedula=cedula,
            nombre=nombre,
            direccion=direccion,
            telefono=telefono
        ))

        datos = {
            "id_usuario": usuario.id,
            "email":      usuario.email,
            "rol":        "CLIENTE",
            "nombre":     cliente.nombre,
            "id_perfil":  cliente.id
        }
        return True, datos, None

    # ─── REGISTRO REPARTIDOR ────────────────────────────────────────
    def registro_repartidor(
        self,
        email: str,
        password: str,
        confirmar_password: str,
        cedula: str,
        nombre: str,
        correo_contacto: str,
        direccion: str,
        telefono: str,
        tarjeta: str
    ) -> Tuple[bool, Optional[dict], Optional[str]]:

        email           = (email or "").strip().lower()
        password        = (password or "").strip()
        cedula          = (cedula or "").strip()
        nombre          = (nombre or "").strip()
        correo_contacto = (correo_contacto or email).strip()

        if not all([email, password, cedula, nombre, direccion, telefono, tarjeta]):
            return False, None, "Todos los campos son requeridos"
        if password != (confirmar_password or "").strip():
            return False, None, "Las contraseñas no coinciden"
        if len(password) < 6:
            return False, None, "La contraseña debe tener al menos 6 caracteres"
        if self._usuarios.existe_email(email):
            return False, None, "Ese correo ya está registrado"
        if self._repartidores.existe_cedula(cedula):
            return False, None, "Esa cédula ya está registrada"

        usuario = self._usuarios.crear(Usuario(
            email=email,
            password_hash=hash_password(password),
            rol="REPARTIDOR",
            estado=1
        ))

        repartidor = self._repartidores.crear(Repartidor(
            usuario_id=usuario.id,
            cedula=cedula,
            nombre=nombre,
            correo=correo_contacto,
            direccion=direccion,
            telefono=telefono,
            tarjeta=tarjeta
        ))

        datos = {
            "id_usuario": usuario.id,
            "email":      usuario.email,
            "rol":        "REPARTIDOR",
            "nombre":     repartidor.nombre,
            "id_perfil":  repartidor.id
        }
        return True, datos, None

    # ─── Helper ─────────────────────────────────────────────────────
    def _obtener_perfil(self, usuario: Usuario) -> Tuple[str, int]:
        if usuario.rol == "CLIENTE":
            perfil = self._clientes.encontrar_por_usuario_id(usuario.id)
        elif usuario.rol == "REPARTIDOR":
            perfil = self._repartidores.encontrar_por_usuario_id(usuario.id)
        else:
            return usuario.email, -1

        if perfil:
            return perfil.nombre, perfil.id
        return usuario.email, -1
