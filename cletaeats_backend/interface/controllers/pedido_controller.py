# interface/controllers/pedido_controller.py
from core.use_cases.pedido_use_cases import PedidoUseCases
from services.session_service import SessionService
from interface.controllers._base_response import send_json, get_token


class PedidoController:

    def __init__(self, pedido_uc: PedidoUseCases, session: SessionService):
        self._uc      = pedido_uc
        self._session = session

    def handle_crear(self, handler, body: dict) -> None:
        """POST /pedidos — requiere token CLIENTE"""
        datos = self._session.validar(get_token(handler))
        if datos is None:
            send_json(handler, 401, {"error": "No autenticado"})
            return
        if datos["rol"] != "CLIENTE":
            send_json(handler, 403, {"error": "Solo los clientes pueden crear pedidos"})
            return

        ok, factura, error = self._uc.crear_pedido(
            id_cliente     = datos["id_perfil"],
            id_restaurante = body.get("restaurante_id"),
            items_input    = body.get("items", []),
            distancia_km   = body.get("distancia_km", 5.0)
        )
        if not ok:
            send_json(handler, 400, {"error": error})
            return
        send_json(handler, 201, factura)

    def handle_listar_cliente(self, handler) -> None:
        """GET /pedidos/cliente — requiere token CLIENTE"""
        datos = self._session.validar(get_token(handler))
        if datos is None or datos["rol"] != "CLIENTE":
            send_json(handler, 401, {"error": "No autenticado o rol incorrecto"})
            return
        send_json(handler, 200, self._uc.obtener_pedidos_cliente(datos["id_perfil"]))

    def handle_listar_repartidor(self, handler) -> None:
        """GET /pedidos/repartidor — requiere token REPARTIDOR"""
        datos = self._session.validar(get_token(handler))
        if datos is None or datos["rol"] != "REPARTIDOR":
            send_json(handler, 401, {"error": "No autenticado o rol incorrecto"})
            return
        send_json(handler, 200, self._uc.obtener_pedidos_repartidor(datos["id_perfil"]))

    def handle_entregar(self, handler, id_pedido: int) -> None:
        """PUT /pedidos/{id}/entregar — requiere token REPARTIDOR"""
        datos = self._session.validar(get_token(handler))
        if datos is None or datos["rol"] != "REPARTIDOR":
            send_json(handler, 401, {"error": "No autenticado o rol incorrecto"})
            return

        ok, error = self._uc.marcar_entregado(id_pedido, datos["id_perfil"])
        if not ok:
            send_json(handler, 400, {"error": error})
            return
        send_json(handler, 200, {"mensaje": f"Pedido #{id_pedido} marcado como entregado"})

    def handle_factura(self, handler, id_pedido: int) -> None:
        """GET /pedidos/{id}/factura — requiere token"""
        if not self._session.validar(get_token(handler)):
            send_json(handler, 401, {"error": "No autenticado"})
            return
        factura = self._uc.obtener_factura(id_pedido)
        if factura is None:
            send_json(handler, 404, {"error": "Pedido no encontrado"})
            return
        send_json(handler, 200, factura)