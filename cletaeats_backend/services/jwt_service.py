# services/jwt_service.py
import os
from datetime import datetime, timedelta, timezone
from typing import Optional

from jose import JWTError, jwt

SECRET_KEY: str = os.environ.get(
    "JWT_SECRET_KEY",
    "cletaeats-dev-secret-cambia-esto-en-produccion-2026"
)
ALGORITHM   = "HS256"
EXPIRE_DAYS = 7


def crear_token(
    id_usuario: int,
    email:      str,
    rol:        str,
    nombre:     str,
    id_perfil:  int
) -> str:
    payload = {
        "sub":       str(id_usuario),
        "email":     email,
        "rol":       rol,
        "nombre":    nombre,
        "id_perfil": id_perfil,
        "exp":       datetime.now(timezone.utc) + timedelta(days=EXPIRE_DAYS),
    }
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def validar_token(token: str) -> Optional[dict]:
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return {
            "id_usuario": int(payload["sub"]),
            "email":      payload["email"],
            "rol":        payload["rol"],
            "nombre":     payload["nombre"],
            "id_perfil":  payload["id_perfil"],
        }
    except JWTError:
        return None