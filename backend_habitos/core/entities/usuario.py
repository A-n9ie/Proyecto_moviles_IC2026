# core/entities/usuario.py

class Usuario:
    
    def __init__(
        self,
        id: int = None,
        nombre_usuario: str = "",
        email: str = "",
        password: str = "",
        estado_usuario: int = None
    ):
        self.id = id
        self.nombre_usuario = nombre_usuario
        self.email = email
        self.password = password
        self.estado_usuario = estado_usuario

    def __str__(self):
        return f"Usuario(id={self.id}, nombre={self.nombre_usuario})"

    def __repr__(self):
        return self.__str__()