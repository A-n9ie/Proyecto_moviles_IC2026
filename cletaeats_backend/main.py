# main.py
from http.server import HTTPServer

from data.repositories.usuario_repository    import UsuarioRepository
from data.repositories.cliente_repository    import ClienteRepository
from data.repositories.repartidor_repository import RepartidorRepository
from data.repositories.restaurante_repository import RestauranteRepository
from data.repositories.combo_repository      import ComboRepository
from data.repositories.pedido_repository     import PedidoRepository

from core.use_cases.auth_use_cases        import AuthUseCases
from core.use_cases.restaurante_use_cases import RestauranteUseCases
from core.use_cases.combo_use_cases       import ComboUseCases
from core.use_cases.pedido_use_cases      import PedidoUseCases

from services.session_service import SessionService

from interface.controllers.auth_controller        import AuthController
from interface.controllers.restaurante_controller import RestauranteController
from interface.controllers.combo_controller       import ComboController
from interface.controllers.pedido_controller      import PedidoController
from interface.controllers.request_handler        import RequestHandler
from interface.controllers.admin_controller       import AdminController

# ================================================================
# EMULADOR (activo):  HOST = "localhost"
#   Android usa: http://10.0.2.2:8000
#
# DISPOSITIVO FÍSICO:
#   1. Cambiar HOST = "0.0.0.0"
#   2. Android usa: http://<IP_LOCAL_PC>:8000
# ================================================================
HOST = "0.0.0.0"
PORT = 8000


def crear_app() -> type:
    # ── Capa DATA ────────────────────────────────────────────────
    usuario_repo    = UsuarioRepository()
    cliente_repo    = ClienteRepository()
    repartidor_repo = RepartidorRepository()
    restaurante_repo = RestauranteRepository()
    combo_repo      = ComboRepository()
    pedido_repo     = PedidoRepository()

    # ── Servicios ────────────────────────────────────────────────
    session_svc = SessionService()

    # ── Use Cases ────────────────────────────────────────────────
    auth_uc         = AuthUseCases(usuario_repo, cliente_repo, repartidor_repo)
    restaurante_uc  = RestauranteUseCases(restaurante_repo)
    combo_uc        = ComboUseCases(combo_repo, restaurante_repo)
    pedido_uc       = PedidoUseCases(pedido_repo, combo_repo, restaurante_repo, repartidor_repo)

    # ── Controllers ──────────────────────────────────────────────
    auth_ctrl        = AuthController(auth_uc, session_svc)
    restaurante_ctrl = RestauranteController(restaurante_uc, session_svc)
    combo_ctrl       = ComboController(combo_uc, session_svc)
    pedido_ctrl      = PedidoController(pedido_uc, session_svc)
    admin_ctrl = AdminController({
        "cliente":     cliente_repo,
        "repartidor":  repartidor_repo,
        "restaurante": restaurante_repo,
        "combo":       combo_repo,
        "pedido":      pedido_repo
    }, session_svc)

    # ── Inyección en el handler ───────────────────────────────────
    RequestHandler.auth_controller        = auth_ctrl
    RequestHandler.restaurante_controller = restaurante_ctrl
    RequestHandler.combo_controller       = combo_ctrl
    RequestHandler.pedido_controller      = pedido_ctrl
    RequestHandler.admin_controller       = admin_ctrl

    return RequestHandler


def run():
    handler = crear_app()
    server  = HTTPServer((HOST, PORT), handler)
    print("\n✅  CletaEats Backend — Fase 2")
    print(f"    http://{HOST}:{PORT}\n")
    print("    POST  /auth/login")
    print("    POST  /auth/registro/cliente")
    print("    POST  /auth/registro/repartidor")
    print("    POST  /auth/logout")
    print("    GET   /restaurantes")
    print("    GET   /combos?restaurante={id}")
    print("    POST  /pedidos")
    print("    GET   /pedidos/{id}/factura")
    print("    GET   /pedidos/cliente")
    print("    GET   /pedidos/repartidor")
    print("    PUT   /pedidos/{id}/entregar")
    print("\n    Ctrl+C para detener.\n")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n⛔  Servidor detenido.")
        server.server_close()


if __name__ == "__main__":
    run()