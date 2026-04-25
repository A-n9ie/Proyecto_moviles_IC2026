# interface/controllers/request_handler.py
import json
import re
from http.server import BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
from interface.controllers._base_response import send_json


class RequestHandler(BaseHTTPRequestHandler):
    """
    Router HTTP completo. Único archivo que extiende BaseHTTPRequestHandler.
    Todos los controllers se inyectan desde main.py.
    """

    # Inyectados en main.py ────────────────────────────────────────
    auth_controller        = None
    restaurante_controller = None
    combo_controller       = None
    pedido_controller      = None

    # ── CORS preflight ────────────────────────────────────────────
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin",  "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    # ── GET ───────────────────────────────────────────────────────
    def do_GET(self):
        parsed = urlparse(self.path)
        path   = parsed.path
        query  = parse_qs(parsed.query)

        # GET /restaurantes
        if path == "/restaurantes":
            self.restaurante_controller.handle_listar(self)
            return

        # GET /combos?restaurante={id}
        if path == "/combos":
            restaurante_id = query.get("restaurante", [None])[0]
            if not restaurante_id:
                send_json(self, 400, {"error": "Parámetro 'restaurante' requerido"})
                return
            self.combo_controller.handle_listar(self, int(restaurante_id))
            return

        # GET /pedidos/cliente
        if path == "/pedidos/cliente":
            self.pedido_controller.handle_listar_cliente(self)
            return

        # GET /pedidos/repartidor
        if path == "/pedidos/repartidor":
            self.pedido_controller.handle_listar_repartidor(self)
            return

        # GET /pedidos/{id}/factura
        m = re.match(r"^/pedidos/(\d+)/factura$", path)
        if m:
            self.pedido_controller.handle_factura(self, int(m.group(1)))
            return

        send_json(self, 404, {"error": f"Ruta GET '{path}' no existe"})

    # ── POST ──────────────────────────────────────────────────────
    def do_POST(self):
        path = urlparse(self.path).path
        body = self._leer_body()

        rutas = {
            "/auth/login":               lambda: self.auth_controller.handle_login(self, body),
            "/auth/registro/cliente":    lambda: self.auth_controller.handle_registro_cliente(self, body),
            "/auth/registro/repartidor": lambda: self.auth_controller.handle_registro_repartidor(self, body),
            "/auth/logout":              lambda: self.auth_controller.handle_logout(self),
            "/pedidos":                  lambda: self.pedido_controller.handle_crear(self, body),
        }

        if path in rutas:
            rutas[path]()
            return

        send_json(self, 404, {"error": f"Ruta POST '{path}' no existe"})

    # ── PUT ───────────────────────────────────────────────────────
    def do_PUT(self):
        path = urlparse(self.path).path

        # PUT /pedidos/{id}/entregar
        m = re.match(r"^/pedidos/(\d+)/entregar$", path)
        if m:
            self.pedido_controller.handle_entregar(self, int(m.group(1)))
            return

        send_json(self, 404, {"error": f"Ruta PUT '{path}' no existe"})

    # ── Helpers ───────────────────────────────────────────────────
    def _leer_body(self) -> dict:
        try:
            length = int(self.headers.get("Content-Length", 0))
            return json.loads(self.rfile.read(length).decode("utf-8")) if length else {}
        except Exception:
            return {}

    def log_message(self, fmt, *args):
        print(f"  [{self.command:7}] {self.path}  →  {fmt % args}")