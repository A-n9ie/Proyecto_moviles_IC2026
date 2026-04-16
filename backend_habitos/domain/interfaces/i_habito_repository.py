# domain/interfaces/i_habito_repository.py
from abc import ABC, abstractmethod
from typing import List, Optional
from core.entities.habito import Habito


class IHabitoRepository(ABC):
    # CRUD completo para entidad Habito.

    @abstractmethod
    def obtener_por_usuario(self, id_usuario: int) -> List[Habito]:
        pass

    @abstractmethod
    def obtener_por_id(self, id_habito: int) -> Optional[Habito]:
        pass

    @abstractmethod
    def crear(self, habito: Habito) -> Habito:
        pass

    @abstractmethod
    def actualizar(self, habito: Habito) -> Habito:
        pass

    @abstractmethod
    def eliminar(self, id_habito: int) -> bool:
        # Retorna True si se eliminó, False si no existía."""
        pass