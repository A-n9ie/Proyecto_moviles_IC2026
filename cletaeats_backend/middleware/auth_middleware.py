# middleware/auth_middleware.py
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from services.jwt_service import validar_token

_bearer = HTTPBearer()


def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(_bearer)
) -> dict:
    sesion = validar_token(credentials.credentials)
    if sesion is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido o expirado",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return sesion


def require_admin(sesion: dict = Depends(get_current_user)) -> dict:
    if sesion.get("rol") not in ("ADMIN", "ADMINISTRADOR"):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acceso restringido a administradores",
        )
    return sesion


def require_cliente(sesion: dict = Depends(get_current_user)) -> dict:
    if sesion.get("rol") != "CLIENTE":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acceso restringido a clientes",
        )
    return sesion


def require_repartidor(sesion: dict = Depends(get_current_user)) -> dict:
    if sesion.get("rol") != "REPARTIDOR":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acceso restringido a repartidores",
        )
    return sesion