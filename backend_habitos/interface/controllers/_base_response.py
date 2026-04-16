# interface/controllers/_base_response.py
import json


def send_json(handler, status_code: int, data) -> None:
    # Serializa datos a JSON y escribe la respuesta HTTP completa.
    # Incluye headers CORS para aceptar conexiones del emulador Android.
    # Centralizado aquí para que todos los controllers usen exactamente
    # los mismos headers sin duplicar código.
    
    body = json.dumps(data, ensure_ascii=False).encode("utf-8")

    handler.send_response(status_code)
    handler.send_header("Content-Type", "application/json; charset=utf-8")
    handler.send_header("Content-Length", str(len(body)))
    handler.send_header("Access-Control-Allow-Origin", "*")
    handler.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
    handler.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
    handler.end_headers()
    handler.wfile.write(body)


def get_token(handler) -> str:
    # Extrae el Bearer token del header Authorization.
    # Formato esperado: "Authorization: Bearer <token_hex>"
    # Retorna string vacío si no está presente o mal formado.
    
    auth = handler.headers.get("Authorization", "")
    if auth.startswith("Bearer "):
        return auth[7:].strip()
    return ""