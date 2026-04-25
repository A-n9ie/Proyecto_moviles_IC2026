# domain/interfaces/i_restaurante_repository.py
from abc import ABC, abstractmethod
from typing import List, Optional
from core.entities.restaurante import Restaurante


class IRestauranteRepository(ABC):

    @abstractmethod
    def obtener_todos_activos(self) -> List[Restaurante]:
        pass

    @abstractmethod
    def obtener_por_id(self, id_restaurante: int) -> Optional[Restaurante]:
        pass