# data/database/db_connection.py
import sqlite3
import os

# ================================================================
# __file__ está en: cletaeats_backend/data/database/db_connection.py
# Subir 3 niveles llega a:  ./
# El archivo .db se guarda en: cletaeats_data/cletaeats.db
# ================================================================
_BASE_DIR = os.path.normpath(
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..")
)
DB_PATH = os.path.join(_BASE_DIR, "cletaeats_data", "cletaeats.db")


def get_connection() -> sqlite3.Connection:
    """
    Retorna conexión SQLite con foreign_keys activado.
    FUTURO: Reemplazar este cuerpo para migrar a PostgreSQL/MySQL
            sin tocar ningún repositorio.
    """
    conn = sqlite3.connect(DB_PATH)
    conn.execute("PRAGMA foreign_keys = ON")
    conn.row_factory = sqlite3.Row   # permite acceso por nombre de columna
    return conn