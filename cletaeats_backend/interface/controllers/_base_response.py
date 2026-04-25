# interface/controllers/_base_response.py
import json


def send_json(handler, status: int, data) -> None:
    """Serializa y envía respuesta JSON con headers CORS."""
    body = json.dumps(data, ensure_ascii=False).encode("utf-8")
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json; charset=utf-8")
    handler.send_header("Content-Length", str(len(body)))
    handler.send_header("Access-Control-Allow-Origin",  "*")
    handler.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
    handler.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
    handler.end_headers()
    handler.wfile.write(body)


def get_token(handler) -> str:
    """Extrae Bearer token del header Authorization."""
    auth = handler.headers.get("Authorization", "")
    return auth[7:].strip() if auth.startswith("Bearer ") else ""