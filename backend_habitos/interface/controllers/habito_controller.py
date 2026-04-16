# interface/controllers/habito_controller.py
from urllib.parse import urlparse, parse_qs
from core.use_cases.habito_use_cases import HabitoUseCases
from services.session_service import SessionService
from interface.controllers._base_response import send_json, get_token


class HabitoController:
    # Maneja el CRUD completo de hábitos.

    def __init__(self, habito_uc: HabitoUseCases, session_service: SessionService):
        self._uc      = habito_uc
        self._session = session_service

    # ---- GET /habitos ----
    def handle_listar(self, handler, full_path: str) -> None:
        id_usuario = self._session.get_id_usuario(get_token(handler))

        # Compatibilidad retroactiva: acepta ?usuario=N mientras el frontend
        # no envíe token. Útil durante la transición del lab.
        if id_usuario is None:
            param = parse_qs(urlparse(full_path).query).get("usuario", [None])[0]
            if param is None:
                send_json(handler, 401, {"error": "No autenticado"})
                return
            id_usuario = int(param)

        try:
            habitos = self._uc.obtener_habitos_usuario(id_usuario)
            send_json(handler, 200, [self._to_dict(h) for h in habitos])
        except ValueError as e:
            send_json(handler, 400, {"error": str(e)})

    # ---- GET /habitos/{id} ----
    def handle_obtener(self, handler, id_habito: int) -> None:
        if not self._autenticado(handler):
            send_json(handler, 401, {"error": "No autenticado"})
            return
        try:
            send_json(handler, 200, self._to_dict(self._uc.obtener_habito(id_habito)))
        except ValueError as e:
            send_json(handler, 404, {"error": str(e)})

    # ---- POST /habitos ----
    def handle_crear(self, handler, body: dict) -> None:
        id_usuario = self._session.get_id_usuario(get_token(handler))
        if id_usuario is None:
            send_json(handler, 401, {"error": "No autenticado"})
            return
        try:
            habito = self._uc.crear_habito(
                nombre=body.get("nombre", ""),
                descripcion=body.get("descripcion", ""),
                id_usuario=id_usuario,
                id_tipo=body.get("id_tipo"),
                id_estado=body.get("id_estado"),
                duracion_objetivo=body.get("duracion_objetivo")
            )
            send_json(handler, 201, self._to_dict(habito))
        except ValueError as e:
            send_json(handler, 400, {"error": str(e)})

    # ---- PUT /habitos/{id} ----
    def handle_actualizar(self, handler, id_habito: int, body: dict) -> None:
        if not self._autenticado(handler):
            send_json(handler, 401, {"error": "No autenticado"})
            return
        try:
            habito = self._uc.actualizar_habito(
                id_habito=id_habito,
                nombre=body.get("nombre", ""),
                descripcion=body.get("descripcion", ""),
                id_tipo=body.get("id_tipo"),
                id_estado=body.get("id_estado"),
                duracion_objetivo=body.get("duracion_objetivo")
            )
            send_json(handler, 200, self._to_dict(habito))
        except ValueError as e:
            send_json(handler, 404, {"error": str(e)})

    # ---- DELETE /habitos/{id} ----
    def handle_eliminar(self, handler, id_habito: int) -> None:
        if not self._autenticado(handler):
            send_json(handler, 401, {"error": "No autenticado"})
            return
        try:
            self._uc.eliminar_habito(id_habito)
            send_json(handler, 200, {"mensaje": f"Hábito {id_habito} eliminado"})
        except ValueError as e:
            send_json(handler, 404, {"error": str(e)})

    # ---- Helpers ----
    def _autenticado(self, handler) -> bool:
        return self._session.validar_sesion(get_token(handler)) is not None

    @staticmethod
    def _to_dict(h) -> dict:
        return {
            "id":                h.id,
            "nombre":            h.nombre,
            "descripcion":       h.descripcion,
            "id_usuario":        h.id_usuario,
            "id_tipo":           h.id_codigo_tipo_habito,
            "id_estado":         h.estado_habito,
            "duracion_objetivo": h.duracion_objetivo
        }