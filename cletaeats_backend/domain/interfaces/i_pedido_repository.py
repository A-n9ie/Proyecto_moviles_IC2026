# domain/interfaces/i_pedido_repository.py
from abc import ABC, abstractmethod
from typing import List, Optional
from core.entities.pedido import Pedido


class IPedidoRepository(ABC):

    @abstractmethod
    def crear_con_detalles(self, pedido: Pedido, items: list) -> Pedido:
        """
        Crea PEDIDO + todos sus DETALLE_PEDIDO en una sola transacción.
        items: [{'combo_id': int, 'cantidad': int, 'precio_unitario': float, 'configuracion': str}]
        Retorna el pedido con su ID asignado.
        """
        pass

    @abstractmethod
    def obtener_por_id(self, pedido_id: int) -> Optional[Pedido]:
        pass

    @abstractmethod
    def obtener_factura(self, pedido_id: int) -> Optional[dict]:
        """
        Retorna dict completo con JOIN de todas las tablas necesarias
        para mostrar la factura al cliente.
        """
        pass

    @abstractmethod
    def obtener_por_repartidor(self, repartidor_id: int) -> List[dict]:
        """Pedidos asignados al repartidor con estado != ENTREGADO ni CANCELADO."""
        pass

    @abstractmethod
    def obtener_por_cliente(self, cliente_id: int) -> List[dict]:
        """Historial de pedidos del cliente."""
        pass

    @abstractmethod
    def actualizar_estado(
        self, pedido_id: int, nuevo_estado: int, fecha_entrega: str = None
    ) -> bool:
        pass