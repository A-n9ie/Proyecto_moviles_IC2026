# interface/controllers/request_handler.py
import json
import re
from http.server import BaseHTTPRequestHandler
from urllib.parse import urlparse
from interface.controllers._base_response import send_json


class RequestHandler(BaseHTTPRequestHandler):
    """
    Router HTTP. Único archivo que extiende BaseHTTPRequestHandler.
    Controllers inyectados desde main.py como atributos de clase.
    """
    auth_controller        = None
    restaurante_controller = None

    # ── CORS preflight ─────────────────────────────────────────────
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin",  "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    # ── GET ────────────────────────────────────────────────────────
    def do_GET(self):
        path = urlparse(self.path).path

        if path == "/restaurantes":
            self.restaurante_controller.handle_listar(self)
            return

        send_json(self, 404, {"error": f"Ruta GET '{path}' no existe"})

    # ── POST ───────────────────────────────────────────────────────
    def do_POST(self):
        path = urlparse(self.path).path
        body = self._leer_body()

        rutas = {
            "/auth/login":               lambda: self.auth_controller.handle_login(self, body),
            "/auth/registro/cliente":    lambda: self.auth_controller.handle_registro_cliente(self, body),
            "/auth/registro/repartidor": lambda: self.auth_controller.handle_registro_repartidor(self, body),
            "/auth/logout":              lambda: self.auth_controller.handle_logout(self),
        }

        if path in rutas:
            rutas[path]()
            return

        send_json(self, 404, {"error": f"Ruta POST '{path}' no existe"})

    # ── Helpers ────────────────────────────────────────────────────
    def _leer_body(self) -> dict:
        try:
            length = int(self.headers.get("Content-Length", 0))
            return json.loads(self.rfile.read(length).decode("utf-8")) if length else {}
        except Exception:
            return {}

    def log_message(self, fmt, *args):
        print(f"  [{self.command:7}] {self.path}  →  {fmt % args}")