# core/entities/habito.py

class Habito:
    
    def __init__(
        self,
        id: int = None,
        nombre: str = "",
        descripcion: str = "",
        id_usuario: int = None,
        id_codigo_tipo_habito: int = None,
        estado_habito: int = None,
        duracion_objetivo: int = None
    ):
        self.id = id
        self.nombre = nombre
        self.descripcion = descripcion
        self.id_usuario = id_usuario
        self.id_codigo_tipo_habito = id_codigo_tipo_habito
        self.estado_habito = estado_habito
        self.duracion_objetivo = duracion_objetivo

    # --- Getters y setters ---

    def get_nombre(self) -> str:
        return self.nombre

    def set_nombre(self, nombre: str):
        self.nombre = nombre

    def get_descripcion(self) -> str:
        return self.descripcion

    def set_descripcion(self, descripcion: str):
        self.descripcion = descripcion

    def __str__(self):
        return f"Habito(id={self.id}, nombre={self.nombre}, usuario={self.id_usuario})"

    def __repr__(self):
        return self.__str__()