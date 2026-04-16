# domain/interfaces/i_usuario_repository.py
from abc import ABC, abstractmethod
from typing import Optional
from core.entities.usuario import Usuario


class IUsuarioRepository(ABC):

    @abstractmethod
    def encontrar_por_nombre_usuario(self, nombre_usuario: str) -> Optional[Usuario]:
        # Busca por nombre exacto. Retorna None si no existe.
        pass

    @abstractmethod
    def crear(self, usuario: Usuario) -> Usuario:
        # Persiste el usuario y retorna el mismo objeto con el ID asignado.
        pass

    @abstractmethod
    def existe(self, nombre_usuario: str) -> bool:
        # Retorna True si ya existe un usuario con ese nombre.
        pass