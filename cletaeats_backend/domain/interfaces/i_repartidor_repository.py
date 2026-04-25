# domain/interfaces/i_repartidor_repository.py
from abc import ABC, abstractmethod
from typing import Optional, List
from core.entities.repartidor import Repartidor


class IRepartidorRepository(ABC):

    @abstractmethod
    def crear(self, repartidor: Repartidor) -> Repartidor:
        pass

    @abstractmethod
    def encontrar_por_usuario_id(self, usuario_id: int) -> Optional[Repartidor]:
        pass

    @abstractmethod
    def existe_cedula(self, cedula: str) -> bool:
        pass

    @abstractmethod
    def obtener_primero_disponible(self) -> Optional[Repartidor]:
        """Retorna el primer repartidor disponible con < 4 amonestaciones."""
        pass
    
    @abstractmethod
    def actualizar_estado(self, id_repartidor: int, estado: int) -> bool:
        """
        Cambia el estado del repartidor.
        1 = disponible (al entregar pedido)
        0 = ocupado    (al asignarle un pedido)
        """
        pass