# interface/controllers/request_handler.py
import json
import re
from http.server import BaseHTTPRequestHandler
from urllib.parse import urlparse
from interface.controllers._base_response import send_json


class RequestHandler(BaseHTTPRequestHandler):
    """
    Router HTTP principal. El ÚNICO archivo que extiende BaseHTTPRequestHandler.
    Delega cada ruta a su controller. No contiene lógica de negocio.

    Los controllers se inyectan desde main.py como atributos de clase,
    patrón necesario porque HTTPServer instancia RequestHandler por request
    y no permite pasar argumentos al constructor.
    """

    # Inyectados en main.py
    auth_controller    = None
    habito_controller  = None

    # ---- Métodos HTTP ----

    def do_OPTIONS(self):
        """Responde al CORS preflight del emulador o dispositivo Android."""
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    def do_GET(self):
        path = urlparse(self.path).path

        if path == "/habitos":
            self.habito_controller.handle_listar(self, self.path)
            return

        m = re.match(r"^/habitos/(\d+)$", path)
        if m:
            self.habito_controller.handle_obtener(self, int(m.group(1)))
            return

        send_json(self, 404, {"error": f"Ruta GET '{path}' no existe"})

    def do_POST(self):
        path = urlparse(self.path).path
        body = self._leer_body()

        rutas = {
            "/auth/login":    lambda: self.auth_controller.handle_login(self, body),
            "/auth/registro": lambda: self.auth_controller.handle_registro(self, body),
            "/auth/logout":   lambda: self.auth_controller.handle_logout(self),
            "/habitos":       lambda: self.habito_controller.handle_crear(self, body),
        }

        if path in rutas:
            rutas[path]()
            return

        send_json(self, 404, {"error": f"Ruta POST '{path}' no existe"})

    def do_PUT(self):
        path = urlparse(self.path).path
        body = self._leer_body()

        m = re.match(r"^/habitos/(\d+)$", path)
        if m:
            self.habito_controller.handle_actualizar(self, int(m.group(1)), body)
            return

        send_json(self, 404, {"error": f"Ruta PUT '{path}' no existe"})

    def do_DELETE(self):
        path = urlparse(self.path).path

        m = re.match(r"^/habitos/(\d+)$", path)
        if m:
            self.habito_controller.handle_eliminar(self, int(m.group(1)))
            return

        send_json(self, 404, {"error": f"Ruta DELETE '{path}' no existe"})

    # ---- Helpers privados ----

    def _leer_body(self) -> dict:
        """Lee y parsea el JSON del body. Retorna {} si está vacío o es inválido."""
        try:
            length = int(self.headers.get("Content-Length", 0))
            if length == 0:
                return {}
            return json.loads(self.rfile.read(length).decode("utf-8"))
        except (json.JSONDecodeError, UnicodeDecodeError, ValueError):
            return {}

    def log_message(self, fmt, *args):
        """Override: log más limpio en consola."""
        print(f"  [{self.command:6}] {self.path}  →  {fmt % args}")