# core/use_cases/pedido_use_cases.py
from typing import List, Tuple, Optional
from datetime import datetime

from domain.interfaces.i_pedido_repository     import IPedidoRepository
from domain.interfaces.i_combo_repository      import IComboRepository
from domain.interfaces.i_restaurante_repository import IRestauranteRepository
from domain.interfaces.i_repartidor_repository  import IRepartidorRepository
from core.entities.pedido import Pedido


class PedidoUseCases:
    """
    Lógica de negocio completa de pedidos.
    No conoce SQLite, HTTP ni UI.
    """

    def __init__(
        self,
        pedido_repo:      IPedidoRepository,
        combo_repo:       IComboRepository,
        restaurante_repo: IRestauranteRepository,
        repartidor_repo:  IRepartidorRepository
    ):
        self._pedidos      = pedido_repo
        self._combos       = combo_repo
        self._restaurantes = restaurante_repo
        self._repartidores = repartidor_repo

    # ─── CREAR PEDIDO ─────────────────────────────────────────────
    def crear_pedido(
        self,
        id_cliente:     int,
        id_restaurante: int,
        items_input:    List[dict],
        distancia_km:   float
    ) -> Tuple[bool, Optional[dict], Optional[str]]:
        """
        items_input: [{'combo_id': int, 'cantidad': int, 'configuracion': str}]
        Returns: (success, factura_dict | None, error_msg | None)
        """
        # ── Validaciones de entrada ────────────────────────────────
        if not items_input:
            return False, None, "El pedido debe contener al menos un combo"
        if not id_restaurante or id_restaurante <= 0:
            return False, None, "Restaurante inválido"
        try:
            distancia_km = float(distancia_km)
        except (TypeError, ValueError):
            return False, None, "Distancia inválida"
        if distancia_km <= 0:
            return False, None, "La distancia debe ser mayor a 0 km"

        # ── Validar restaurante ────────────────────────────────────
        restaurante = self._restaurantes.obtener_por_id(id_restaurante)
        if restaurante is None or restaurante.estado == 0:
            return False, None, "El restaurante no está disponible"

        # ── Validar y enriquecer items con precios ─────────────────
        items_procesados = []
        for raw in items_input:
            combo_id = raw.get("combo_id")
            cantidad = int(raw.get("cantidad", 1))

            if not combo_id or cantidad <= 0:
                return False, None, f"Item inválido: combo_id={combo_id}, cantidad={cantidad}"

            combo = self._combos.obtener_por_id(combo_id)
            if combo is None:
                return False, None, f"Combo {combo_id} no existe"
            if combo.restaurante_id != id_restaurante:
                return False, None, f"El combo '{combo.nombre}' no pertenece a este restaurante"
            if combo.estado == 0:
                return False, None, f"El combo '{combo.nombre}' no está disponible"

            items_procesados.append({
                "combo_id":        combo.id,
                "cantidad":        cantidad,
                "precio_unitario": combo.precio,
                "configuracion":   raw.get("configuracion", "{}")
            })

        # ── Asignar repartidor ─────────────────────────────────────
        repartidor = self._repartidores.obtener_primero_disponible()
        if repartidor is None:
            return False, None, "No hay repartidores disponibles en este momento"

        # ── Persistir ─────────────────────────────────────────────
        pedido = Pedido(
            cliente_id     = id_cliente,
            restaurante_id = id_restaurante,
            repartidor_id  = repartidor.id,
            estado         = 0,             # CREADO
            distancia_km   = distancia_km
        )
        try:
            pedido = self._pedidos.crear_con_detalles(pedido, items_procesados)
            # Marcar repartidor como ocupado
            self._repartidores.actualizar_campos(repartidor.id, {"estado": 0})
            # Generar y retornar factura
            factura = self._pedidos.obtener_factura(pedido.id)
            return True, factura, None
        except Exception as exc:
            return False, None, f"Error al guardar el pedido: {exc}"

    # ─── CONSULTAS ────────────────────────────────────────────────
    def obtener_pedidos_cliente(self, id_cliente: int) -> List[dict]:
        return self._pedidos.obtener_por_cliente(id_cliente)

    def obtener_pedidos_repartidor(self, id_repartidor: int) -> List[dict]:
        return self._pedidos.obtener_por_repartidor(id_repartidor)

    def obtener_factura(self, id_pedido: int) -> Optional[dict]:
        return self._pedidos.obtener_factura(id_pedido)

    # ─── ENTREGAR ─────────────────────────────────────────────────
    def marcar_entregado(
        self, id_pedido: int, id_repartidor: int
    ) -> Tuple[bool, Optional[str]]:
        pedido = self._pedidos.obtener_por_id(id_pedido)
        if pedido is None:
            return False, "Pedido no encontrado"
        if pedido.repartidor_id != id_repartidor:
            return False, "Este pedido no te pertenece"
        if pedido.estado in (3, 4):
            return False, "El pedido ya fue entregado o cancelado"

        fecha_entrega = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        ok = self._pedidos.actualizar_estado(id_pedido, 3, fecha_entrega)
        if ok:
            self._repartidores.actualizar_campos(id_repartidor, {"estado": 1})  # vuelve a disponible  # vuelve a disponible
        return ok, None if ok else "No se pudo actualizar el pedido"