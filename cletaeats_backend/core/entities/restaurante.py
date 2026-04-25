# core/entities/restaurante.py

class Restaurante:
    """
    Restaurante registrado en el sistema.
    El profesor especificó 7 restaurantes activos inicialmente.
    """
    def __init__(
        self,
        id: int = None,
        cedula_juridica: str = "",
        nombre: str = "",
        direccion: str = "",
        tipo_comida: str = "",
        estado: int = 1,
        imagen_url: str = ""
    ):
        self.id              = id
        self.cedula_juridica = cedula_juridica
        self.nombre          = nombre
        self.direccion       = direccion
        self.tipo_comida     = tipo_comida
        self.estado          = estado
        self.imagen_url      = imagen_url

    def get_nombre(self) -> str: return self.nombre
    def set_nombre(self, v: str): self.nombre = v

    def __str__(self):
        return f"Restaurante(id={self.id}, nombre={self.nombre})"