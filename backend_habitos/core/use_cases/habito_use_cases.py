# core/use_cases/habito_use_cases.py
from typing import List, Optional
from domain.interfaces.i_habito_repository import IHabitoRepository
from core.entities.habito import Habito

# ============================================================
# Valores por defecto del catálogo para nuevos hábitos:
#   ID_CODIGO_TIPO_HABITO = 10  → Fijo
#   ESTADO_HABITO = 1   → Activo
#
# TODO: Consultar desde CatalogRepository en fases futuras.
# ============================================================
_TIPO_DEFAULT   = 10   # Fijo
_ESTADO_DEFAULT = 1    # Activo


class HabitoUseCases:
    # Casos de uso para CRUD completo de hábitos. Depende SOLO de IHabitoRepository.

    def __init__(self, habito_repo: IHabitoRepository):
        self._repo = habito_repo

    def obtener_habitos_usuario(self, id_usuario: int) -> List[Habito]:
        if not id_usuario or id_usuario <= 0:
            raise ValueError("ID de usuario inválido")
        return self._repo.obtener_por_usuario(id_usuario)

    def obtener_habito(self, id_habito: int) -> Habito:
        if not id_habito or id_habito <= 0:
            raise ValueError("ID de hábito inválido")
        habito = self._repo.obtener_por_id(id_habito)
        if habito is None:
            raise ValueError(f"Hábito con ID {id_habito} no encontrado")
        return habito

    def crear_habito(
        self,
        nombre: str,
        descripcion: str,
        id_usuario: int,
        id_tipo: Optional[int] = None,
        id_estado: Optional[int] = None,
        duracion_objetivo: Optional[int] = None
    ) -> Habito:
        nombre = (nombre or "").strip()
        if not nombre:
            raise ValueError("El nombre del hábito es requerido")
        if not id_usuario or id_usuario <= 0:
            raise ValueError("ID de usuario inválido")

        habito = Habito(
            nombre=nombre,
            descripcion=(descripcion or "").strip(),
            id_usuario=id_usuario,
            id_codigo_tipo_habito=id_tipo or _TIPO_DEFAULT,
            estado_habito=id_estado or _ESTADO_DEFAULT,
            duracion_objetivo=duracion_objetivo
        )
        return self._repo.crear(habito)

    def actualizar_habito(
        self,
        id_habito: int,
        nombre: str,
        descripcion: str,
        id_tipo: Optional[int] = None,
        id_estado: Optional[int] = None,
        duracion_objetivo: Optional[int] = None
    ) -> Habito:
        nombre = (nombre or "").strip()
        if not nombre:
            raise ValueError("El nombre del hábito es requerido")

        habito = self._repo.obtener_por_id(id_habito)
        if habito is None:
            raise ValueError(f"Hábito con ID {id_habito} no encontrado")

        habito.nombre = nombre
        habito.descripcion = (descripcion or "").strip()
        if id_tipo is not None:
            habito.id_codigo_tipo_habito = id_tipo
        if id_estado is not None:
            habito.estado_habito = id_estado
        if duracion_objetivo is not None:
            habito.duracion_objetivo = duracion_objetivo

        return self._repo.actualizar(habito)

    def eliminar_habito(self, id_habito: int) -> bool:
        habito = self._repo.obtener_por_id(id_habito)
        if habito is None:
            raise ValueError(f"Hábito con ID {id_habito} no encontrado")
        return self._repo.eliminar(id_habito)