# data/database/db_connection.py
import sqlite3
import os

# ================================================================
# Ruta absoluta al archivo db.sqlite calculada dinámicamente.
#
# __file__ está en: backend_habitos/data/database/db_connection.py
# Subir 2 niveles llega a:  backend_habitos/
# Luego bajar a:            backend_habitos/data/db.sqlite
#
# Así el servidor funciona sin importar desde qué directorio
# se ejecute el comando "python main.py"
# ================================================================
_BASE_DIR = os.path.normpath(
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..","..")
)
DB_PATH = os.path.join(_BASE_DIR, "data", "habitos.db")


def get_connection() -> sqlite3.Connection:
    """
    Abre y retorna una nueva conexión SQLite.
    Activa foreign_keys para garantizar integridad referencial.

    FUTURO: Para migrar a otra BD, reemplazar solo esta función.
    No hay ningún otro archivo en el proyecto que importe sqlite3.
    """
    conn = sqlite3.connect(DB_PATH)
    conn.execute("PRAGMA foreign_keys = ON")
    return conn