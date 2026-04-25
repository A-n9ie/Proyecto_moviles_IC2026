# core/use_cases/restaurante_use_cases.py
from typing import List
from domain.interfaces.i_restaurante_repository import IRestauranteRepository
from core.entities.restaurante import Restaurante


class RestauranteUseCases:

    def __init__(self, restaurante_repo: IRestauranteRepository):
        self._repo = restaurante_repo

    def obtener_restaurantes_activos(self) -> List[Restaurante]:
        """Solo CLIENTES autenticados deben llamar este endpoint."""
        return self._repo.obtener_todos_activos()

    def obtener_por_id(self, id_restaurante: int) -> Restaurante:
        r = self._repo.obtener_por_id(id_restaurante)
        if r is None:
            raise ValueError(f"Restaurante {id_restaurante} no encontrado")
        return r