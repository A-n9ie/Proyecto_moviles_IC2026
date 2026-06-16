# data/database/db_connection.py
import os
from sqlalchemy import create_engine, event
from sqlalchemy import MetaData

_BASE_DIR = os.path.normpath(
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..")
)
DB_PATH = os.environ.get(
    "DB_PATH",
    os.path.join(_BASE_DIR, "cletaeats_data", "cletaeats.db")
)


engine = create_engine(
    f"sqlite:///{DB_PATH}",
    connect_args={"check_same_thread": False},
    echo=False,
)

@event.listens_for(engine, "connect")
def _set_sqlite_pragma(dbapi_connection, connection_record):
    cursor = dbapi_connection.cursor()
    cursor.execute("PRAGMA foreign_keys = ON")
    cursor.close()

metadata = MetaData()