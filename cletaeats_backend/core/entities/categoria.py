# core/entities/categoria.py

class Categoria:
    def __init__(self, id: int = None, nombre: str = ""):
        self.id     = id
        self.nombre = nombre

    def __str__(self):
        return f"Categoria(id={self.id}, nombre={self.nombre})"