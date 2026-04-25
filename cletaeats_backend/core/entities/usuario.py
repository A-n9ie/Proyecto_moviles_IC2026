# core/entities/usuario.py

class Usuario:
    """
    Entidad central de autenticación.
    ROL: 'CLIENTE' | 'REPARTIDOR' | 'ADMIN' | 'RESTAURANTE'
    ESTADO: 1 = activo, 0 = suspendido
    """
    def __init__(
        self,
        id: int = None,
        email: str = "",
        password_hash: str = "",
        rol: str = "",
        estado: int = 1,
        fecha_registro: str = None
    ):
        self.id            = id
        self.email         = email
        self.password_hash = password_hash
        self.rol           = rol
        self.estado        = estado
        self.fecha_registro = fecha_registro

    def __str__(self):
        return f"Usuario(id={self.id}, email={self.email}, rol={self.rol})"

    def es_activo(self) -> bool:
        return self.estado == 1