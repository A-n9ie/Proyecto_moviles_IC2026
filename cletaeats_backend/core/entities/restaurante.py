# core/entities/restaurante.py

class Restaurante:
    def __init__(
        self,
        id: int = None,
        cedula_juridica: str = "",
        nombre: str = "",
        direccion: str = "",
        estado: int = 1,
        imagen_url: str = "",
        categorias: list = None,
        latitud: float = None,
        longitud: float = None
    ):
        self.id              = id
        self.cedula_juridica = cedula_juridica
        self.nombre          = nombre
        self.direccion       = direccion
        self.estado          = estado
        self.imagen_url      = imagen_url
        self.categorias      = categorias or []
        self.latitud         = latitud
        self.longitud        = longitud

    def get_nombre(self) -> str: return self.nombre
    def set_nombre(self, v: str): self.nombre = v

    def __str__(self):
        return f"Restaurante(id={self.id}, nombre={self.nombre})"