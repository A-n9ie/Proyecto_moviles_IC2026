# core/use_cases/auth_use_cases.py
from typing import Tuple, Optional
from domain.interfaces.i_usuario_repository import IUsuarioRepository
from core.entities.usuario import Usuario

# ============================================================
# ID del estado "Activo" en la tabla CODIGO.
# Según el catálogo insertado en la BD:
#   DIA_SEMANA ocupa IDs 1–7
#   ESTADO_HABITO: Activo = 8, Inactivo = 9
#
# TODO: En fases futuras, obtener este valor desde un
#       CatalogRepository (no hardcodeado).
# ============================================================
ESTADO_USUARIO = 1


class AuthUseCases:
    # asos de uso de autenticación. Depende SOLO de IUsuarioRepository (interfaz abstracta), nunca de SQLite.

    def __init__(self, usuario_repo: IUsuarioRepository):
        self._repo = usuario_repo

    def login(
        self, nombre_usuario: str, password: str
    ) -> Tuple[bool, Optional[Usuario], Optional[str]]:
        
        # Valida credenciales.

        # Returns:
        #     (True,  usuario, None)        — login exitoso
        #     (False, None,   mensaje)      — falla con motivo
        
        nombre_usuario = (nombre_usuario or "").strip()
        password = (password or "").strip()

        if not nombre_usuario:
            return False, None, "El nombre de usuario es requerido"
        if not password:
            return False, None, "La contraseña es requerida"

        usuario = self._repo.encontrar_por_nombre_usuario(nombre_usuario)
        if usuario is None:
            return False, None, "Usuario no encontrado"

        # TODO fase futura: comparar hash (bcrypt). Hoy es texto plano
        # para mantener consistencia con la BD actual del lab.
        if usuario.password != password:
            return False, None, "Contraseña incorrecta"

        return True, usuario, None

    def registro(
        self,
        nombre_usuario: str,
        email: str,
        password: str,
        confirmar_password: str
    ) -> Tuple[bool, Optional[Usuario], Optional[str]]:
        # Registra un nuevo usuario.

        # Devuelve:
        #    (True,  usuario, None)     — registro exitoso
        #    (False, None,   mensaje)   — falla con motivo
        
        nombre_usuario     = (nombre_usuario or "").strip()
        email              = (email or "").strip()
        password           = (password or "").strip()
        confirmar_password = (confirmar_password or "").strip()

        if not nombre_usuario or not email or not password:
            return False, None, "Todos los campos son requeridos"
        if len(nombre_usuario) < 4:
            return False, None, "El usuario debe tener al menos 4 caracteres"
        if len(password) < 4:
            return False, None, "La contraseña debe tener al menos 4 caracteres"
        if password != confirmar_password:
            return False, None, "Las contraseñas no coinciden"
        if self._repo.existe(nombre_usuario):
            return False, None, "Ese nombre de usuario ya está en uso"

        nuevo = Usuario(
            nombre_usuario=nombre_usuario,
            email=email,
            password=password,
            estado_usuario=ESTADO_USUARIO
        )
        creado = self._repo.crear(nuevo)
        return True, creado, None