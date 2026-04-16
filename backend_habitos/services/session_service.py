# services/session_service.py
import secrets
import threading
from typing import Optional, Dict


class SessionService:
    # Gestión de sesiones activas en memoria.
    # Almacena: { token: {"id_usuario": int, "nombre_usuario": str} }

    # Thread-safe gracias al Lock (HTTPServer usa hilos por request).

    # FUTURO: Reemplazar con JWTSessionService o RedisSessionService
    #         implementando exactamente los mismos métodos públicos.

    def __init__(self):
        self._sessions: Dict[str, dict] = {}
        self._lock = threading.Lock()

    def crear_sesion(self, id_usuario: int, nombre_usuario: str) -> str:
        # Genera un token criptográficamente seguro y registra la sesión.
        token = secrets.token_hex(32)
        with self._lock:
            self._sessions[token] = {
                "id_usuario": id_usuario,
                "nombre_usuario": nombre_usuario
            }
        return token

    def validar_sesion(self, token: Optional[str]) -> Optional[dict]:
        # Retorna los datos de sesión o None si el token no existe o es vacío.
        if not token:
            return None
        with self._lock:
            return self._sessions.get(token)

    def eliminar_sesion(self, token: Optional[str]) -> bool:
        # Cierra sesión. Retorna True si el token existía.
        if not token:
            return False
        with self._lock:
            if token in self._sessions:
                del self._sessions[token]
                return True
        return False

    def get_id_usuario(self, token: Optional[str]) -> Optional[int]:
        # Obtiene el ID de usuario asociado al token activo.
        session = self.validar_sesion(token)
        return session["id_usuario"] if session else None