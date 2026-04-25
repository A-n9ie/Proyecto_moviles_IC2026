# main.py
from http.server import HTTPServer

from data.repositories.usuario_repository    import UsuarioRepository
from data.repositories.cliente_repository    import ClienteRepository
from data.repositories.repartidor_repository import RepartidorRepository
from data.repositories.restaurante_repository import RestauranteRepository

from core.use_cases.auth_use_cases        import AuthUseCases
from core.use_cases.restaurante_use_cases import RestauranteUseCases

from services.session_service import SessionService

from interface.controllers.auth_controller        import AuthController
from interface.controllers.restaurante_controller import RestauranteController
from interface.controllers.request_handler        import RequestHandler

# ================================================================
# EMULADOR ANDROID (actual):
#   HOST = "localhost"
#   En Android usar: http://10.0.2.2:8000
#
# DISPOSITIVO FÍSICO (cuando se migre):
#   1. Cambiar HOST = "0.0.0.0"
#   2. En Android cambiar BASE_URL a http://<IP_LOCAL_PC>:8000
#   3. En network_security_config.xml agregar la IP
# ================================================================
HOST = "localhost"
PORT = 8000


def crear_app() -> type:
    # ── Capa DATA ──────────────────────────────────────────────────
    usuario_repo    = UsuarioRepository()
    cliente_repo    = ClienteRepository()
    repartidor_repo = RepartidorRepository()
    restaurante_repo = RestauranteRepository()

    # ── Servicios ──────────────────────────────────────────────────
    session_svc = SessionService()

    # ── Use Cases ──────────────────────────────────────────────────
    auth_uc       = AuthUseCases(usuario_repo, cliente_repo, repartidor_repo)
    restaurante_uc = RestauranteUseCases(restaurante_repo)

    # ── Controllers ────────────────────────────────────────────────
    auth_ctrl        = AuthController(auth_uc, session_svc)
    restaurante_ctrl = RestauranteController(restaurante_uc, session_svc)

    # ── Inyección en handler ───────────────────────────────────────
    RequestHandler.auth_controller        = auth_ctrl
    RequestHandler.restaurante_controller = restaurante_ctrl

    return RequestHandler


def run():
    handler = crear_app()
    server  = HTTPServer((HOST, PORT), handler)

    print("\n✅  CletaEats Backend iniciado")
    print(f"    http://{HOST}:{PORT}\n")
    print("    POST  /auth/login")
    print("    POST  /auth/registro/cliente")
    print("    POST  /auth/registro/repartidor")
    print("    POST  /auth/logout")
    print("    GET   /restaurantes          ← Bearer token CLIENTE")
    print("\n    Ctrl+C para detener.\n")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n⛔  Servidor detenido.")
        server.server_close()


if __name__ == "__main__":
    run()