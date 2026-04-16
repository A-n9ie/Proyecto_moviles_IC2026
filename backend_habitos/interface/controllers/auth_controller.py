# interface/controllers/auth_controller.py
from core.use_cases.auth_use_cases import AuthUseCases
from services.session_service import SessionService
from interface.controllers._base_response import send_json, get_token


class AuthController:
    # Maneja las rutas de autenticación.
    # Solo traduce HTTP → AuthUseCases → HTTP. Sin lógica de negocio.

    def __init__(self, auth_use_cases: AuthUseCases, session_service: SessionService):
        self._auth   = auth_use_cases
        self._session = session_service

    def handle_login(self, handler, body: dict) -> None:
        """POST /auth/login"""
        ok, usuario, error = self._auth.login(
            body.get("nombre_usuario", ""),
            body.get("password", "")
        )
        if not ok:
            send_json(handler, 401, {"error": error})
            return

        token = self._session.crear_sesion(usuario.id, usuario.nombre_usuario)
        send_json(handler, 200, {
            "token": token,
            "id_usuario": usuario.id,
            "nombre_usuario": usuario.nombre_usuario
        })

    def handle_registro(self, handler, body: dict) -> None:
        """POST /auth/registro"""
        ok, usuario, error = self._auth.registro(
            body.get("nombre_usuario", ""),
            body.get("email", ""),
            body.get("password", ""),
            body.get("confirmar_password", "")
        )
        if not ok:
            send_json(handler, 400, {"error": error})
            return

        token = self._session.crear_sesion(usuario.id, usuario.nombre_usuario)
        send_json(handler, 201, {
            "token": token,
            "id_usuario": usuario.id,
            "nombre_usuario": usuario.nombre_usuario
        })

    def handle_logout(self, handler) -> None:
        """POST /auth/logout"""
        self._session.eliminar_sesion(get_token(handler))
        send_json(handler, 200, {"mensaje": "Sesión cerrada correctamente"})