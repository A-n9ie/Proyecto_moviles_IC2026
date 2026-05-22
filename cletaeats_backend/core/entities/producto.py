# core/entities/producto.py

class Producto:
    def __init__(
        self,
        id: int = None,
        restaurante_id: int = None,
        nombre: str = "",
        descripcion: str = "",
        estado: int = 1
    ):
        self.id             = id
        self.restaurante_id = restaurante_id
        self.nombre         = nombre
        self.descripcion    = descripcion
        self.estado         = estado

    def __str__(self):
        return f"Producto(id={self.id}, nombre={self.nombre})"