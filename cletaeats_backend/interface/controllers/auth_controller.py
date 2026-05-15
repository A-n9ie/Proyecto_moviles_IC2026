# interface/controllers/auth_controller.py
from core.use_cases.auth_use_cases import AuthUseCases
from services.session_service import SessionService
from interface.controllers._base_response import send_json, get_token

ROLES_POR_PLATAFORMA = {
    "WEB": {
        "ADMIN",
        "EMPLEADO"
    },

    "MOBILE": {
        "CLIENTE",
        "REPARTIDOR"
    }
}

class AuthController:
    """Traduce HTTP ↔ AuthUseCases. Sin lógica de negocio."""

    def __init__(self, auth_uc: AuthUseCases, session: SessionService):
        self._auth    = auth_uc
        self._session = session

    def handle_login(self, handler, body: dict) -> None:
        """POST /auth/login"""
        ok, datos, error = self._auth.login(
            body.get("email", ""),
            body.get("password", "")
        )
        if not ok:
            send_json(handler, 401, {"error": error})
            return

        platform = body.get("platform", "MOBILE")

        roles_permitidos = ROLES_POR_PLATAFORMA.get(
            platform,
            set()
        )
        
        # Verificar que el rol es permitido en el móvil
        if datos["rol"] not in roles_permitidos:
            send_json(handler, 403, {
                "error": (
                    f"El rol {datos['rol']} "
                    f"no puede acceder desde {platform}"
                )
            })
            return

        token = self._session.crear_sesion(
            id_usuario=datos["id_usuario"],
            email=datos["email"],
            rol=datos["rol"],
            nombre=datos["nombre"],
            id_perfil=datos["id_perfil"]
        )
        send_json(handler, 200, {
            "token":      token,
            "id_usuario": datos["id_usuario"],
            "email":      datos["email"],
            "rol":        datos["rol"],
            "nombre":     datos["nombre"],
            "id_perfil":  datos["id_perfil"]
        })

    def handle_registro_cliente(self, handler, body: dict) -> None:
        """POST /auth/registro/cliente"""
        ok, datos, error = self._auth.registro_cliente(
            email=body.get("email", ""),
            password=body.get("password", ""),
            confirmar_password=body.get("confirmar_password", ""),
            cedula=body.get("cedula", ""),
            nombre=body.get("nombre", ""),
            direccion=body.get("direccion", ""),
            telefono=body.get("telefono", ""),
            tarjeta=body.get("tarjeta", "")
        )
        if not ok:
            send_json(handler, 400, {"error": error})
            return

        token = self._session.crear_sesion(**datos)
        send_json(handler, 201, {"token": token, **datos})

    def handle_registro_repartidor(self, handler, body: dict) -> None:
        """POST /auth/registro/repartidor"""
        ok, datos, error = self._auth.registro_repartidor(
            email=body.get("email", ""),
            password=body.get("password", ""),
            confirmar_password=body.get("confirmar_password", ""),
            cedula=body.get("cedula", ""),
            nombre=body.get("nombre", ""),
            correo_contacto=body.get("correo_contacto", ""),
            direccion=body.get("direccion", ""),
            telefono=body.get("telefono", ""),
            tarjeta=body.get("tarjeta", "")
        )
        if not ok:
            send_json(handler, 400, {"error": error})
            return

        token = self._session.crear_sesion(**datos)
        send_json(handler, 201, {"token": token, **datos})

    def handle_logout(self, handler) -> None:
        """POST /auth/logout"""
        self._session.eliminar(get_token(handler))
        send_json(handler, 200, {"mensaje": "Sesión cerrada"})