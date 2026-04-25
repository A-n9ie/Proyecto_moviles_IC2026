# services/session_service.py
import secrets
import threading
from typing import Optional, Dict


class SessionService:
    """
    Sesiones en memoria. Thread-safe con Lock.

    Almacena: { token → { id_usuario, email, rol, nombre, id_perfil } }

    FUTURO: Reemplazar por JWT o Redis para escalar a múltiples instancias.
    """

    def __init__(self):
        self._sessions: Dict[str, dict] = {}
        self._lock = threading.Lock()

    def crear_sesion(
        self,
        id_usuario: int,
        email: str,
        rol: str,
        nombre: str,
        id_perfil: int
    ) -> str:
        token = secrets.token_hex(32)
        with self._lock:
            self._sessions[token] = {
                "id_usuario": id_usuario,
                "email":      email,
                "rol":        rol,
                "nombre":     nombre,
                "id_perfil":  id_perfil
            }
        return token

    def validar(self, token: Optional[str]) -> Optional[dict]:
        if not token:
            return None
        with self._lock:
            return self._sessions.get(token)

    def eliminar(self, token: Optional[str]) -> bool:
        if not token:
            return False
        with self._lock:
            if token in self._sessions:
                del self._sessions[token]
                return True
        return False

    def get_datos(self, token: Optional[str]) -> Optional[dict]:
        return self.validar(token)