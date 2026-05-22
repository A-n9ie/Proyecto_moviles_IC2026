# middleware/auth_middleware.py
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from services.session_instance import session_service

_bearer = HTTPBearer()

def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(_bearer)
) -> dict:
    sesion = session_service.validar(credentials.credentials)
    if not sesion:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token inválido o expirado")
    return sesion

def require_admin(sesion: dict = Depends(get_current_user)) -> dict:
    if sesion.get("rol") not in ("ADMIN", "ADMINISTRADOR"):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Acceso restringido a administradores")
    return sesion