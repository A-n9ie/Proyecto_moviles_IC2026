# domain/interfaces/i_tarjeta_cliente_repository.py

from abc import ABC, abstractmethod
from core.entities.tarjeta_cliente import TarjetaCliente

class ITarjetaClienteRepository(ABC):

    @abstractmethod
    def crear(self, tarjeta: TarjetaCliente) -> TarjetaCliente:
        pass