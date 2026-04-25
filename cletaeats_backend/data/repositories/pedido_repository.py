# data/repositories/pedido_repository.py
from typing import List, Optional
from domain.interfaces.i_pedido_repository import IPedidoRepository
from core.entities.pedido import Pedido
from data.database.db_connection import get_connection


class PedidoRepository(IPedidoRepository):

    # ─── CREAR ────────────────────────────────────────────────────
    def crear_con_detalles(self, pedido: Pedido, items: list) -> Pedido:
        conn = get_connection()
        try:
            # 1. Insertar PEDIDO
            cursor = conn.execute(
                """
                INSERT INTO PEDIDO
                    (CLIENTE_ID, RESTAURANTE_ID, REPARTIDOR_ID, ESTADO, DISTANCIA_KM)
                VALUES (?, ?, ?, ?, ?)
                """,
                (
                    pedido.cliente_id, pedido.restaurante_id,
                    pedido.repartidor_id, pedido.estado, pedido.distancia_km
                )
            )
            pedido.id = cursor.lastrowid

            # 2. Insertar DETALLE_PEDIDO (misma transacción)
            for item in items:
                conn.execute(
                    """
                    INSERT INTO DETALLE_PEDIDO
                        (PEDIDO_ID, COMBO_ID, CANTIDAD, PRECIO_UNITARIO, CONFIGURACION)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    (
                        pedido.id,
                        item["combo_id"],
                        item["cantidad"],
                        item["precio_unitario"],
                        item.get("configuracion", "{}")
                    )
                )

            conn.commit()
            return pedido

        except Exception as exc:
            conn.rollback()
            raise exc
        finally:
            conn.close()

    # ─── LEER ─────────────────────────────────────────────────────
    def obtener_por_id(self, pedido_id: int) -> Optional[Pedido]:
        conn = get_connection()
        try:
            row = conn.execute(
                "SELECT * FROM PEDIDO WHERE ID = ?", (pedido_id,)
            ).fetchone()
            return self._fila_a_pedido(row) if row else None
        finally:
            conn.close()

    def obtener_factura(self, pedido_id: int) -> Optional[dict]:
        """
        Genera el dict de factura con todos los datos necesarios:
        info del pedido + nombres + items + totales calculados.
        """
        conn = get_connection()
        try:
            pedido_row = conn.execute(
                """
                SELECT
                    P.ID, P.ESTADO, P.DISTANCIA_KM, P.FECHA_CREACION,
                    C.NOMBRE  AS CLIENTE_NOMBRE,
                    R.NOMBRE  AS RESTAURANTE_NOMBRE,
                    REP.NOMBRE       AS REPARTIDOR_NOMBRE,
                    REP.COSTO_KM_HABIL
                FROM PEDIDO P
                JOIN CLIENTE     C   ON P.CLIENTE_ID     = C.ID
                JOIN RESTAURANTE R   ON P.RESTAURANTE_ID = R.ID
                LEFT JOIN REPARTIDOR REP ON P.REPARTIDOR_ID = REP.ID
                WHERE P.ID = ?
                """,
                (pedido_id,)
            ).fetchone()

            if not pedido_row:
                return None

            item_rows = conn.execute(
                """
                SELECT
                    DP.CANTIDAD, DP.PRECIO_UNITARIO,
                    CO.NOMBRE       AS COMBO_NOMBRE,
                    CO.NUMERO_COMBO
                FROM DETALLE_PEDIDO DP
                JOIN COMBO CO ON DP.COMBO_ID = CO.ID
                WHERE DP.PEDIDO_ID = ?
                ORDER BY CO.NUMERO_COMBO
                """,
                (pedido_id,)
            ).fetchall()

            # Calcular totales
            items      = []
            subtotal   = 0.0
            for row in item_rows:
                item_sub = row["PRECIO_UNITARIO"] * row["CANTIDAD"]
                subtotal += item_sub
                items.append({
                    "combo_nombre":    row["COMBO_NOMBRE"],
                    "numero_combo":    row["NUMERO_COMBO"],
                    "cantidad":        row["CANTIDAD"],
                    "precio_unitario": row["PRECIO_UNITARIO"],
                    "subtotal_item":   round(item_sub, 2)
                })

            costo_km         = pedido_row["COSTO_KM_HABIL"] or 1000.0
            distancia_km     = pedido_row["DISTANCIA_KM"]
            costo_transporte = distancia_km * costo_km
            iva              = subtotal * 0.13
            total            = subtotal + costo_transporte + iva

            return {
                "pedido_id":          pedido_row["ID"],
                "estado":             pedido_row["ESTADO"],
                "restaurante_nombre": pedido_row["RESTAURANTE_NOMBRE"],
                "cliente_nombre":     pedido_row["CLIENTE_NOMBRE"],
                "repartidor_nombre":  pedido_row["REPARTIDOR_NOMBRE"] or "Sin asignar",
                "items":              items,
                "subtotal":           round(subtotal, 2),
                "distancia_km":       distancia_km,
                "costo_transporte":   round(costo_transporte, 2),
                "iva":                round(iva, 2),
                "total":              round(total, 2),
                "fecha_creacion":     pedido_row["FECHA_CREACION"]
            }
        finally:
            conn.close()

    def obtener_por_repartidor(self, repartidor_id: int) -> List[dict]:
        """Pedidos activos del repartidor (excluye ENTREGADO y CANCELADO)."""
        conn = get_connection()
        try:
            rows = conn.execute(
                """
                SELECT
                    P.ID, P.ESTADO, P.DISTANCIA_KM, P.FECHA_CREACION,
                    C.NOMBRE  AS CLIENTE_NOMBRE,
                    R.NOMBRE  AS RESTAURANTE_NOMBRE,
                    R.TIPO_COMIDA,
                    (SELECT COUNT(*) FROM DETALLE_PEDIDO DP
                     WHERE DP.PEDIDO_ID = P.ID) AS ITEMS_COUNT
                FROM PEDIDO P
                JOIN CLIENTE     C ON P.CLIENTE_ID     = C.ID
                JOIN RESTAURANTE R ON P.RESTAURANTE_ID = R.ID
                WHERE P.REPARTIDOR_ID = ? AND P.ESTADO NOT IN (3, 4)
                ORDER BY P.FECHA_CREACION DESC
                """,
                (repartidor_id,)
            ).fetchall()
            return [self._pedido_row_a_dict(r) for r in rows]
        finally:
            conn.close()

    def obtener_por_cliente(self, cliente_id: int) -> List[dict]:
        conn = get_connection()
        try:
            rows = conn.execute(
                """
                SELECT
                    P.ID, P.ESTADO, P.DISTANCIA_KM, P.FECHA_CREACION, P.FECHA_ENTREGA,
                    R.NOMBRE AS RESTAURANTE_NOMBRE,
                    R.TIPO_COMIDA,
                    (SELECT COUNT(*) FROM DETALLE_PEDIDO DP
                     WHERE DP.PEDIDO_ID = P.ID) AS ITEMS_COUNT
                FROM PEDIDO P
                JOIN RESTAURANTE R ON P.RESTAURANTE_ID = R.ID
                WHERE P.CLIENTE_ID = ?
                ORDER BY P.FECHA_CREACION DESC
                """,
                (cliente_id,)
            ).fetchall()
            return [self._pedido_row_a_dict(r) for r in rows]
        finally:
            conn.close()

    # ─── ACTUALIZAR ───────────────────────────────────────────────
    def actualizar_estado(
        self, pedido_id: int, nuevo_estado: int, fecha_entrega: str = None
    ) -> bool:
        conn = get_connection()
        try:
            if fecha_entrega:
                cursor = conn.execute(
                    "UPDATE PEDIDO SET ESTADO = ?, FECHA_ENTREGA = ? WHERE ID = ?",
                    (nuevo_estado, fecha_entrega, pedido_id)
                )
            else:
                cursor = conn.execute(
                    "UPDATE PEDIDO SET ESTADO = ? WHERE ID = ?",
                    (nuevo_estado, pedido_id)
                )
            conn.commit()
            return cursor.rowcount > 0
        finally:
            conn.close()

    # ─── Helpers ──────────────────────────────────────────────────
    @staticmethod
    def _fila_a_pedido(row) -> Pedido:
        return Pedido(
            id             = row["ID"],
            cliente_id     = row["CLIENTE_ID"],
            restaurante_id = row["RESTAURANTE_ID"],
            repartidor_id  = row["REPARTIDOR_ID"],
            fecha_creacion = row["FECHA_CREACION"],
            fecha_entrega  = row["FECHA_ENTREGA"],
            estado         = row["ESTADO"],
            distancia_km   = row["DISTANCIA_KM"]
        )

    @staticmethod
    def _pedido_row_a_dict(row) -> dict:
        return {
            "id":                 row["ID"],
            "estado":             row["ESTADO"],
            "estado_texto":       Pedido.ESTADOS.get(row["ESTADO"], "DESCONOCIDO"),
            "restaurante_nombre": row["RESTAURANTE_NOMBRE"],
            "tipo_comida":        row["TIPO_COMIDA"],
            "cliente_nombre":     row.keys().__contains__("CLIENTE_NOMBRE") and row["CLIENTE_NOMBRE"] or "",
            "distancia_km":       row["DISTANCIA_KM"],
            "fecha_creacion":     row["FECHA_CREACION"],
            "fecha_entrega":      row["FECHA_ENTREGA"] if "FECHA_ENTREGA" in row.keys() else None,
            "items_count":        row["ITEMS_COUNT"]
        }