# domain/interfaces/i_combo_repository.py
from abc import ABC, abstractmethod
from typing import List, Optional
from core.entities.combo import Combo


class IComboRepository(ABC):

    @abstractmethod
    def obtener_por_restaurante(self, restaurante_id: int) -> List[Combo]:
        """Retorna combos activos del restaurante, ordenados por numero_combo."""
        pass

    @abstractmethod
    def obtener_por_id(self, combo_id: int) -> Optional[Combo]:
        pass