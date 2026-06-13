# core/entities/repartidor.py

class Repartidor:
    """
    Perfil de usuario con rol REPARTIDOR.
    DISPONIBLE: 1 = si, 0 = ocupado.
    AMONESTACIONES: 0-3 activo, 4 = fuera de la empresa.
    """
    def __init__(
        self,
        id: int = None,
        usuario_id: int = None,
        cedula: str = "",
        nombre: str = "",
        correo: str = "",
        direccion: str = "",
        telefono: str = "",
        imagen_url: str = "",
        tarjeta: str = "",
        disponible: int = 1,
        km_recorridos_diarios: float = 0.0,
        costo_km_habil: float = 1000.0,
        costo_km_feriado: float = 1500.0,
        amonestaciones: int = 0,
        rating: float = 0.0,
    ):
        self.id                    = id
        self.usuario_id            = usuario_id
        self.cedula                = cedula
        self.nombre                = nombre
        self.correo                = correo
        self.direccion             = direccion
        self.telefono              = telefono
        self.imagen_url            = imagen_url
        self.tarjeta               = tarjeta
        self.disponible            = disponible
        self.km_recorridos_diarios = km_recorridos_diarios
        self.costo_km_habil        = costo_km_habil
        self.costo_km_feriado      = costo_km_feriado
        self.amonestaciones        = amonestaciones
        self.rating                = rating

    def get_nombre(self) -> str: return self.nombre
    def set_nombre(self, v: str): self.nombre = v
    def get_amonestaciones(self) -> int: return self.amonestaciones
    def set_amonestaciones(self, v: int): self.amonestaciones = v

    def esta_disponible(self) -> bool:
        return self.disponible == 1 and self.amonestaciones < 4

    def __str__(self):
        return f"Repartidor(id={self.id}, nombre={self.nombre})"