# core/use_cases/combo_use_cases.py
from typing import List
from domain.interfaces.i_combo_repository import IComboRepository
from domain.interfaces.i_restaurante_repository import IRestauranteRepository


class ComboUseCases:
    """
    Obtiene combos de un restaurante junto con la info del restaurante.
    Un solo endpoint evita dos llamadas HTTP desde el cliente móvil.
    """

    def __init__(
        self,
        combo_repo:       IComboRepository,
        restaurante_repo: IRestauranteRepository
    ):
        self._combos       = combo_repo
        self._restaurantes = restaurante_repo

    def obtener_combos_restaurante(self, restaurante_id: int) -> dict:
        """
        Retorna: {'restaurante': Restaurante, 'combos': List[Combo]}
        Raise ValueError si el restaurante no existe o está inactivo.
        """
        restaurante = self._restaurantes.obtener_por_id(restaurante_id)
        if restaurante is None or restaurante.estado == 0:
            raise ValueError(f"Restaurante {restaurante_id} no disponible")

        combos = self._combos.obtener_por_restaurante(restaurante_id)
        return {"restaurante": restaurante, "combos": combos}