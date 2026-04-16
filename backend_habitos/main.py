# main.py
from http.server import HTTPServer

from data.repositories.usuario_repository import UsuarioRepository
from data.repositories.habito_repository import HabitoRepository
from core.use_cases.auth_use_cases import AuthUseCases
from core.use_cases.habito_use_cases import HabitoUseCases
from services.session_service import SessionService
from interface.controllers.auth_controller import AuthController
from interface.controllers.habito_controller import HabitoController
from interface.controllers.request_handler import RequestHandler

# ================================================================
# CONFIGURACIÓN DEL SERVIDOR
#
# EMULADOR Android (por defecto):
#   HOST = "localhost"
#   En Android usar la URL: http://10.0.2.2:8000
#   (10.0.2.2 es el alias del emulador al localhost del PC)
#
# DISPOSITIVO FÍSICO (cambiar cuando sea necesario):
#   1. Cambiar HOST = "0.0.0.0"  → escucha en todas las interfaces
#   2. En Android cambiar BASE_URL a http://<IP_LOCAL_PC>:8000
#      Ejemplo: http://192.168.1.105:8000
#   3. PC y dispositivo deben estar en la misma red WiFi
#   4. Verificar que el firewall del PC permita el puerto 8000
# ================================================================
HOST = "localhost"
PORT = 8000


def crear_app() -> type:
    """
    Ensambla el sistema completo mediante Dependency Injection manual.

    Árbol de dependencias:
        UsuarioRepository  ──→  AuthUseCases  ──→  AuthController  ──→  RequestHandler
        HabitoRepository   ──→  HabitoUseCases ──→ HabitoController ──→  RequestHandler
        SessionService     ──────────────────────→ AuthController
                           ──────────────────────→ HabitoController
    """
    # Capa DATA
    usuario_repo = UsuarioRepository()
    habito_repo  = HabitoRepository()

    # Servicios
    session_svc = SessionService()

    # Casos de uso
    auth_uc    = AuthUseCases(usuario_repo)
    habito_uc  = HabitoUseCases(habito_repo)

    # Controllers
    auth_ctrl   = AuthController(auth_uc, session_svc)
    habito_ctrl = HabitoController(habito_uc, session_svc)

    # Inyección en el handler (class-level, requerido por HTTPServer)
    RequestHandler.auth_controller   = auth_ctrl
    RequestHandler.habito_controller = habito_ctrl

    return RequestHandler


def run():
    handler = crear_app()
    server  = HTTPServer((HOST, PORT), handler)

    print(f"\n✅  Servidor HabitosApp iniciado")
    print(f"    http://{HOST}:{PORT}\n")
    print(f"    POST   /auth/login")
    print(f"    POST   /auth/registro")
    print(f"    POST   /auth/logout")
    print(f"    GET    /habitos               ← requiere token Bearer")
    print(f"    POST   /habitos               ← requiere token Bearer")
    print(f"    GET    /habitos/{{id}}           ← requiere token Bearer")
    print(f"    PUT    /habitos/{{id}}           ← requiere token Bearer")
    print(f"    DELETE /habitos/{{id}}           ← requiere token Bearer")
    print(f"\n    Ctrl+C para detener.\n")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n⛔  Servidor detenido.")
        server.server_close()


if __name__ == "__main__":
    run()