# domain/interfaces/i_cliente_repository.py
from abc import ABC, abstractmethod
from typing import Optional
from core.entities.cliente import Cliente


class IClienteRepository(ABC):

    @abstractmethod
    def crear(self, cliente: Cliente) -> Cliente:
        pass

    @abstractmethod
    def encontrar_por_usuario_id(self, usuario_id: int) -> Optional[Cliente]:
        pass

    @abstractmethod
    def existe_cedula(self, cedula: str) -> bool:
        pass