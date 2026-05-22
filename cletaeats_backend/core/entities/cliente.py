# core/entities/cliente.py

class Cliente:
    """
    Perfil de usuario con rol CLIENTE.
    Relación 1:1 con Usuario.
    """
    def __init__(
        self,
        id: int = None,
        usuario_id: int = None,
        cedula: str = "",
        nombre: str = "",
        direccion: str = "",
        telefono: str = "",
    ):
        self.id         = id
        self.usuario_id = usuario_id
        self.cedula     = cedula
        self.nombre     = nombre
        self.direccion  = direccion
        self.telefono   = telefono

    # Getters y setters (requeridos por el enunciado del profe)
    def get_nombre(self) -> str: return self.nombre
    def set_nombre(self, v: str): self.nombre = v
    def get_cedula(self) -> str: return self.cedula
    def set_cedula(self, v: str): self.cedula = v

    def __str__(self):
        return f"Cliente(id={self.id}, nombre={self.nombre}, cedula={self.cedula})"