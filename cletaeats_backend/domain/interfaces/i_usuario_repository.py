# domain/interfaces/i_usuario_repository.py
from abc import ABC, abstractmethod
from typing import Optional
from core.entities.usuario import Usuario


class IUsuarioRepository(ABC):

    @abstractmethod
    def encontrar_por_email(self, email: str) -> Optional[Usuario]:
        pass

    @abstractmethod
    def crear(self, usuario: Usuario) -> Usuario:
        pass

    @abstractmethod
    def existe_email(self, email: str) -> bool:
        pass