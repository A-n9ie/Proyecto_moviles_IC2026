# services/hash_service.py
import hashlib

"""
SHA-256 usando hashlib (stdlib de Python — cero dependencias externas).

Por qué SHA-256 y no bcrypt:
  - bcrypt requiere instalación externa (pip install bcrypt)
  - El profesor pide no usar frameworks/librerías externas
  - SHA-256 es criptográficamente seguro para este contexto académico

FUTURO: Reemplazar por bcrypt o argon2 para producción real.
"""


def hash_password(raw: str) -> str:
    """Convierte password en texto plano a SHA-256 hex."""
    return hashlib.sha256(raw.strip().encode("utf-8")).hexdigest()


def verify_password(raw: str, hashed: str) -> bool:
    """Compara password ingresado contra el hash almacenado."""
    return hash_password(raw) == hashed