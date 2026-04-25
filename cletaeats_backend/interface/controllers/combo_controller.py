# interface/controllers/combo_controller.py
from urllib.parse import urlparse, parse_qs
from core.use_cases.combo_use_cases import ComboUseCases
from services.session_service import SessionService
from interface.controllers._base_response import send_json, get_token


class ComboController:
    """GET /combos?restaurante={id} — requiere token (CLIENTE o REPARTIDOR)."""

    def __init__(self, combo_uc: ComboUseCases, session: SessionService):
        self._uc      = combo_uc
        self._session = session

    def handle_listar(self, handler, restaurante_id: int) -> None:
        if not self._session.validar(get_token(handler)):
            send_json(handler, 401, {"error": "No autenticado"})
            return

        try:
            data        = self._uc.obtener_combos_restaurante(restaurante_id)
            restaurante = data["restaurante"]
            send_json(handler, 200, {
                "restaurante": {
                    "id":          restaurante.id,
                    "nombre":      restaurante.nombre,
                    "tipo_comida": restaurante.tipo_comida,
                    "direccion":   restaurante.direccion,
                    "imagen_url":  restaurante.imagen_url
                },
                "combos": [
                    {
                        "id":           c.id,
                        "numero_combo": c.numero_combo,
                        "nombre":       c.nombre,
                        "descripcion":  c.descripcion,
                        "precio":       c.precio,
                        "imagen_url":   c.imagen_url
                    }
                    for c in data["combos"]
                ]
            })
        except ValueError as exc:
            send_json(handler, 404, {"error": str(exc)})