# interface/controllers/admin_controller.py
from interface.controllers._base_response import send_json, get_token
from services.session_service import SessionService

class AdminController:
    def __init__(self, repos: dict, session: SessionService):
        self._repos   = repos
        self._session = session

    def _validar_admin(self, handler) -> bool:
        token = get_token(handler)
        sesion = self._session.obtener(token)
        if not sesion or sesion.get("rol") not in ("ADMIN", "ADMINISTRADOR"):
            send_json(handler, 403, {"error": "Acceso restringido a administradores"})
            return False
        return True

    # ── GET /admin/clientes ──────────────────────────────────────
    def handle_listar_clientes(self, handler):
        if not self._validar_admin(handler): return
        rows = self._repos["cliente"].listar_todos()
        send_json(handler, 200, rows)

    # ── GET /admin/repartidores ──────────────────────────────────
    def handle_listar_repartidores(self, handler):
        if not self._validar_admin(handler): return
        rows = self._repos["repartidor"].listar_todos()
        send_json(handler, 200, rows)

    # ── PUT /admin/repartidores/{id} ─────────────────────────────
    def handle_actualizar_repartidor(self, handler, id_repartidor: int, body: dict):
        if not self._validar_admin(handler): return
        ok = self._repos["repartidor"].actualizar_campos(id_repartidor, body)
        if ok: send_json(handler, 200, {"mensaje": "Repartidor actualizado"})
        else:  send_json(handler, 404, {"error": "Repartidor no encontrado"})

    # ── GET /admin/restaurantes ──────────────────────────────────
    def handle_listar_restaurantes(self, handler):
        if not self._validar_admin(handler): return
        rows = self._repos["restaurante"].listar_todos()
        send_json(handler, 200, rows)

    # ── POST /admin/restaurantes ─────────────────────────────────
    def handle_crear_restaurante(self, handler, body: dict):
        if not self._validar_admin(handler): return
        if not body.get("nombre") or not body.get("tipo_comida") or not body.get("direccion"):
            send_json(handler, 400, {"error": "nombre, tipo_comida y direccion son requeridos"})
            return
        nuevo_id = self._repos["restaurante"].crear(body)
        send_json(handler, 201, {"id": nuevo_id, "mensaje": "Restaurante creado"})

    # ── PUT /admin/restaurantes/{id} ─────────────────────────────
    def handle_actualizar_restaurante(self, handler, id_restaurante: int, body: dict):
        if not self._validar_admin(handler): return
        ok = self._repos["restaurante"].actualizar_campos(id_restaurante, body)
        if ok: send_json(handler, 200, {"mensaje": "Restaurante actualizado"})
        else:  send_json(handler, 404, {"error": "Restaurante no encontrado"})

    # ── GET /admin/combos ────────────────────────────────────────
    def handle_listar_combos(self, handler):
        if not self._validar_admin(handler): return
        rows = self._repos["combo"].listar_todos()
        send_json(handler, 200, rows)

    # ── POST /admin/combos ───────────────────────────────────────
    def handle_crear_combo(self, handler, body: dict):
        if not self._validar_admin(handler): return
        if not body.get("nombre") or not body.get("precio"):
            send_json(handler, 400, {"error": "nombre y precio son requeridos"})
            return
        nuevo_id = self._repos["combo"].crear(body)
        send_json(handler, 201, {"id": nuevo_id, "mensaje": "Combo creado"})

    # ── PUT /admin/combos/{id} ───────────────────────────────────
    def handle_actualizar_combo(self, handler, id_combo: int, body: dict):
        if not self._validar_admin(handler): return
        ok = self._repos["combo"].actualizar_campos(id_combo, body)
        if ok: send_json(handler, 200, {"mensaje": "Combo actualizado"})
        else:  send_json(handler, 404, {"error": "Combo no encontrado"})

    # ── GET /admin/pedidos ───────────────────────────────────────
    def handle_listar_pedidos(self, handler):
        if not self._validar_admin(handler): return
        rows = self._repos["pedido"].listar_todos()
        send_json(handler, 200, rows)