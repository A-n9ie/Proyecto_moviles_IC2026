# interface/controllers/restaurante_controller.py
from core.use_cases.restaurante_use_cases import RestauranteUseCases
from services.session_service import SessionService
from interface.controllers._base_response import send_json, get_token


class RestauranteController:

    def __init__(self, restaurante_uc: RestauranteUseCases, session: SessionService):
        self._uc      = restaurante_uc
        self._session = session

    def handle_listar(self, handler) -> None:
        """GET /restaurantes — requiere token CLIENTE"""
        datos = self._session.validar(get_token(handler))
        if datos is None:
            send_json(handler, 401, {"error": "No autenticado"})
            return
        if datos["rol"] != "CLIENTE":
            send_json(handler, 403, {"error": "Solo los clientes pueden ver restaurantes"})
            return

        restaurantes = self._uc.obtener_restaurantes_activos()
        send_json(handler, 200, [self._to_dict(r) for r in restaurantes])

    @staticmethod
    def _to_dict(r) -> dict:
        return {
            "id":              r.id,
            "nombre":          r.nombre,
            "tipo_comida":     r.tipo_comida,
            "direccion":       r.direccion,
            "imagen_url":      r.imagen_url,
            "estado":          r.estado
        }