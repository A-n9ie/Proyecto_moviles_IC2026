# data/repositories/pedido_repository.py
from typing import Optional
from sqlalchemy import select, insert, update, func
from data.database.db_connection import engine
from data.database.tables import (
    pedido as t_pedido, detalle_pedido as t_detalle,
    cliente as t_cliente, restaurante as t_rest,
    repartidor as t_rep, combo as t_combo
)
from core.entities.pedido import Pedido
from data.utils.mapper_utils import to_lower_dict


_ESTADO_TEXTO = {0: "CREADO", 1: "PREPARANDO", 2: "EN_CAMINO", 3: "ENTREGADO", 4: "CANCELADO"}

class PedidoRepository:

    def crear_con_detalles(self, pedido: Pedido, items: list) -> Pedido:
        with engine.begin() as conn:
            result = conn.execute(insert(t_pedido).values(
                CLIENTE_ID=pedido.cliente_id,
                RESTAURANTE_ID=pedido.restaurante_id,
                REPARTIDOR_ID=pedido.repartidor_id,
                ESTADO=pedido.estado,
                DISTANCIA_KM=pedido.distancia_km
            ))
            pedido.id = result.inserted_primary_key[0]
            for item in items:
                conn.execute(insert(t_detalle).values(
                    PEDIDO_ID=pedido.id,
                    COMBO_ID=item["combo_id"],
                    CANTIDAD=item["cantidad"],
                    PRECIO_UNITARIO=item["precio_unitario"],
                    CONFIGURACION=item.get("configuracion", "{}")
                ))
        return pedido

    def obtener_por_id(self, pedido_id: int) -> Optional[Pedido]:
        with engine.connect() as conn:
            row = conn.execute(
                select(t_pedido).where(t_pedido.c.ID == pedido_id)
            ).mappings().first()
            return self._map(row) if row else None


    def listar_por_cliente(self, cliente_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.FECHA_ENTREGA,
                    t_pedido.c.DISTANCIA_KM,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_rest.c.LATITUD.label("RESTAURANTE_LATITUD"),
                    t_rest.c.LONGITUD.label("RESTAURANTE_LONGITUD")
                )
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .where(t_pedido.c.CLIENTE_ID == cliente_id)
                .order_by(t_pedido.c.FECHA_CREACION.desc())
            ).mappings().all()

            # Contar items por pedido
            pedido_ids = [r["ID"] for r in rows]
            conteos = {}
            if pedido_ids:
                cnt_rows = conn.execute(
                    select(
                        t_detalle.c.PEDIDO_ID,
                        func.sum(t_detalle.c.CANTIDAD).label("ITEMS_COUNT")
                    )
                    .where(t_detalle.c.PEDIDO_ID.in_(pedido_ids))
                    .group_by(t_detalle.c.PEDIDO_ID)
                ).mappings().all()
                conteos = {r["PEDIDO_ID"]: int(r["ITEMS_COUNT"]) for r in cnt_rows}

            result = []
            for r in rows:
                d = to_lower_dict(r)
                d["estado_texto"]        = _ESTADO_TEXTO.get(d["estado"], "DESCONOCIDO")
                d["tipo_comida"]         = ""
                d["cliente_nombre"]      = ""
                d["items_count"]         = conteos.get(d["id"], 0)
                d["fecha_entrega"]       = d.get("fecha_entrega") or ""
                d["restaurante_latitud"] = d.get("restaurante_latitud")
                d["restaurante_longitud"]= d.get("restaurante_longitud")
                result.append(d)
            return result

    def listar_por_repartidor(self, repartidor_id: int) -> list:
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.CALIFICADO,
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.DISTANCIA_KM,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_rest.c.LATITUD.label("RESTAURANTE_LATITUD"),
                    t_rest.c.LONGITUD.label("RESTAURANTE_LONGITUD"),
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_cliente.c.DIRECCION.label("CLIENTE_DIRECCION")
                )
                .join(t_rest,    t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .join(t_cliente, t_pedido.c.CLIENTE_ID     == t_cliente.c.ID)
                .where(t_pedido.c.REPARTIDOR_ID == repartidor_id)
                .where(t_pedido.c.ESTADO.in_([0, 1, 2]))
                .order_by(t_pedido.c.FECHA_CREACION)
            ).mappings().all()

            # Obtener conteo de ítems por pedido en una sola consulta adicional
            pedido_ids = [r["ID"] for r in rows]
            conteos = {}
            if pedido_ids:
                cnt_rows = conn.execute(
                    select(
                        t_detalle.c.PEDIDO_ID,
                        func.sum(t_detalle.c.CANTIDAD).label("ITEMS_COUNT")
                    )
                    .where(t_detalle.c.PEDIDO_ID.in_(pedido_ids))
                    .group_by(t_detalle.c.PEDIDO_ID)
                ).mappings().all()
                conteos = {r["PEDIDO_ID"]: int(r["ITEMS_COUNT"]) for r in cnt_rows}

            result = []
            for r in rows:
                d = to_lower_dict(r)
                d["estado_texto"] = _ESTADO_TEXTO.get(d["estado"], "DESCONOCIDO")
                d["tipo_comida"]  = ""
                d["items_count"]  = conteos.get(d["id"], 0)
                d["calificado"] = bool(d.get("calificado", 0))
                result.append(d)
            return result
    
    #filtra ESTADO == 3 (entregados) y ordena por FECHA_ENTREGA descendente (lo más reciente arriba). Trae la distancia de cada pedido, que es lo que vamos a usar para calcular km totales y ganancias en la app.  
    def listar_entregados_por_repartidor(self, repartidor_id: int) -> list:
        """Historial de entregas: pedidos en estado ENTREGADO (3) de un repartidor."""
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.FECHA_ENTREGA,
                    t_pedido.c.DISTANCIA_KM,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_cliente.c.DIRECCION.label("CLIENTE_DIRECCION")
                )
                .join(t_rest,    t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .join(t_cliente, t_pedido.c.CLIENTE_ID     == t_cliente.c.ID)
                .where(t_pedido.c.REPARTIDOR_ID == repartidor_id)
                .where(t_pedido.c.ESTADO == 3)
                .order_by(t_pedido.c.FECHA_ENTREGA.desc())
            ).mappings().all()
            result = []
            for r in rows:
                d = to_lower_dict(r)
                d["estado_texto"]  = _ESTADO_TEXTO.get(d["estado"], "DESCONOCIDO")
                d["tipo_comida"]   = ""
                d["items_count"]   = 0
                d["fecha_entrega"] = d.get("fecha_entrega") or ""
                result.append(d)
            return result

    def listar_todos(self) -> list:
        from sqlalchemy import func
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.DISTANCIA_KM,
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    func.coalesce(
                        func.sum(t_detalle.c.CANTIDAD), 0
                    ).label("ITEMS_COUNT")
                )
                .join(t_cliente, t_pedido.c.CLIENTE_ID     == t_cliente.c.ID)
                .join(t_rest,    t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .outerjoin(t_detalle, t_detalle.c.PEDIDO_ID == t_pedido.c.ID)
                .group_by(
                    t_pedido.c.ID, t_pedido.c.ESTADO,
                    t_pedido.c.FECHA_CREACION, t_pedido.c.DISTANCIA_KM,
                    t_cliente.c.NOMBRE, t_rest.c.NOMBRE
                )
                .order_by(t_pedido.c.FECHA_CREACION.desc())
            ).mappings().all()

            result = []
            for r in rows:
                d = to_lower_dict(r)
                d["estado_texto"] = _ESTADO_TEXTO.get(d["estado"], "DESCONOCIDO")
                result.append(d)
            return result

    def actualizar_estado(self, pedido_id: int, estado: int, fecha_entrega: str = None) -> bool:
        values = {"ESTADO": estado}
        if fecha_entrega:
            values["FECHA_ENTREGA"] = fecha_entrega
        with engine.begin() as conn:
            result = conn.execute(
                update(t_pedido).where(t_pedido.c.ID == pedido_id).values(**values)
            )
            return result.rowcount > 0

    def obtener_factura(self, pedido_id: int) -> Optional[dict]:
        with engine.connect() as conn:
            row = conn.execute(
                select(
                    t_pedido.c.ID,
                    t_pedido.c.ESTADO,
                    t_pedido.c.DISTANCIA_KM,
                    t_pedido.c.FECHA_CREACION,
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE"),
                    t_rep.c.NOMBRE.label("REPARTIDOR_NOMBRE"),
                    t_rep.c.COSTO_KM_HABIL
                )
                .join(t_cliente, t_pedido.c.CLIENTE_ID     == t_cliente.c.ID)
                .join(t_rest,    t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .outerjoin(t_rep, t_pedido.c.REPARTIDOR_ID == t_rep.c.ID)
                .where(t_pedido.c.ID == pedido_id)
            ).mappings().first()
            if not row:
                return None

            items = conn.execute(
                select(
                    t_detalle.c.CANTIDAD,
                    t_detalle.c.PRECIO_UNITARIO,
                    t_combo.c.NOMBRE.label("COMBO_NOMBRE"),
                    t_combo.c.NUMERO_COMBO
                )
                .join(t_combo, t_detalle.c.COMBO_ID == t_combo.c.ID)
                .where(t_detalle.c.PEDIDO_ID == pedido_id)
            ).mappings().all()

            subtotal = sum(i["CANTIDAD"] * i["PRECIO_UNITARIO"] for i in items)
            costo_km = (row["COSTO_KM_HABIL"] or 1000) * row["DISTANCIA_KM"]
            # IVA del 13% sobre el subtotal (la comida).
            # Decisión de diseño: el impuesto aplica al producto, no al
            # servicio de transporte, igual que el desglose de Uber Eats.
            iva = round(subtotal * 0.13)
            total = subtotal + costo_km + iva
            return {
                "pedido_id":      row["ID"],   # nombre que espera la app móvil
                "id_pedido":      row["ID"],   # se mantiene por compatibilidad con la web
                "estado":         row["ESTADO"],
                "cliente":        row["CLIENTE_NOMBRE"],
                "restaurante":    row["RESTAURANTE_NOMBRE"],
                "repartidor":     row["REPARTIDOR_NOMBRE"],
                "fecha_creacion": row["FECHA_CREACION"],
                "distancia_km":   row["DISTANCIA_KM"],
                "items": [
                    {
                        "combo_nombre":    i["COMBO_NOMBRE"],
                        "numero_combo":    i["NUMERO_COMBO"],
                        "cantidad":        i["CANTIDAD"],
                        "precio_unitario": i["PRECIO_UNITARIO"],
                        "subtotal_item":   i["CANTIDAD"] * i["PRECIO_UNITARIO"]  # lo que espera la app
                    }
                    for i in items
                ],
                "subtotal":    subtotal,
                "costo_envio": costo_km,
                "iva":         iva,
                "total":       total
            }

    def restaurante_mas_pedidos(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(t_rest.c.NOMBRE, func.count(t_pedido.c.ID).label("TOTAL"))
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .group_by(t_rest.c.ID)
                .order_by(func.count(t_pedido.c.ID).desc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}

    def restaurante_menos_pedidos(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(t_rest.c.NOMBRE, func.count(t_pedido.c.ID).label("TOTAL"))
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .group_by(t_rest.c.ID)
                .order_by(func.count(t_pedido.c.ID).asc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}

    def monto_por_restaurante(self) -> list:
        from sqlalchemy import func
        with engine.connect() as conn:
            rows = conn.execute(
                select(
                    t_rest.c.NOMBRE,
                    func.sum(t_detalle.c.CANTIDAD * t_detalle.c.PRECIO_UNITARIO).label("MONTO_TOTAL")
                )
                .select_from(t_pedido)
                .join(t_detalle, t_detalle.c.PEDIDO_ID == t_pedido.c.ID)
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .group_by(t_rest.c.ID)
                .order_by(func.sum(t_detalle.c.CANTIDAD * t_detalle.c.PRECIO_UNITARIO).desc())
            ).mappings().all()
            return [to_lower_dict(r) for r in rows]

    def cliente_top(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(t_cliente.c.NOMBRE, func.count(t_pedido.c.ID).label("TOTAL"))
                .join(t_cliente, t_pedido.c.CLIENTE_ID == t_cliente.c.ID)
                .group_by(t_cliente.c.ID)
                .order_by(func.count(t_pedido.c.ID).desc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}

    def hora_pico(self) -> dict:
        from sqlalchemy import func
        with engine.connect() as conn:
            row = conn.execute(
                select(
                    func.strftime("%H", t_pedido.c.FECHA_CREACION).label("HORA"),
                    func.count(t_pedido.c.ID).label("TOTAL")
                )
                .group_by(func.strftime("%H", t_pedido.c.FECHA_CREACION))
                .order_by(func.count(t_pedido.c.ID).desc())
                .limit(1)
            ).mappings().first()
            return to_lower_dict(row) if row else {}

    def monto_total_global(self) -> dict:
            """
            Reporte (k): monto total vendido por TODOS los restaurantes juntos.
            Suma cantidad * precio_unitario de todas las líneas de pedido.
            """
            from sqlalchemy import func
            with engine.connect() as conn:
                total = conn.execute(
                    select(
                        func.coalesce(
                            func.sum(t_detalle.c.CANTIDAD * t_detalle.c.PRECIO_UNITARIO),
                            0
                        ).label("MONTO_TOTAL")
                    )
                ).scalar()
                return {"monto_total": total or 0}

    def pedidos_por_cliente(self) -> list:
        """
        Reporte (n): listado detallado de pedidos agrupados por cliente.
        Cada cliente trae su lista de pedidos con id, fecha, restaurante,
        estado y monto (suma de cantidad * precio_unitario de ese pedido).
        """
        with engine.connect() as conn:
            # Monto por pedido (subtotal de la comida de cada pedido)
            montos_rows = conn.execute(
                select(
                    t_detalle.c.PEDIDO_ID,
                    func.sum(t_detalle.c.CANTIDAD * t_detalle.c.PRECIO_UNITARIO).label("MONTO")
                ).group_by(t_detalle.c.PEDIDO_ID)
            ).mappings().all()
            montos = {r["PEDIDO_ID"]: r["MONTO"] for r in montos_rows}

            # Todos los pedidos con su cliente y restaurante
            rows = conn.execute(
                select(
                    t_cliente.c.ID.label("CLIENTE_ID"),
                    t_cliente.c.NOMBRE.label("CLIENTE_NOMBRE"),
                    t_cliente.c.CEDULA.label("CLIENTE_CEDULA"),
                    t_pedido.c.ID.label("PEDIDO_ID"),
                    t_pedido.c.FECHA_CREACION,
                    t_pedido.c.ESTADO,
                    t_rest.c.NOMBRE.label("RESTAURANTE_NOMBRE")
                )
                .join(t_cliente, t_pedido.c.CLIENTE_ID == t_cliente.c.ID)
                .join(t_rest, t_pedido.c.RESTAURANTE_ID == t_rest.c.ID)
                .order_by(t_cliente.c.NOMBRE, t_pedido.c.FECHA_CREACION.desc())
            ).mappings().all()

            # Agrupar por cliente
            agrupado = {}
            for r in rows:
                cid = r["CLIENTE_ID"]
                if cid not in agrupado:
                    agrupado[cid] = {
                        "cliente_id":     cid,
                        "cliente_nombre": r["CLIENTE_NOMBRE"],
                        "cliente_cedula": r["CLIENTE_CEDULA"],
                        "total_pedidos":  0,
                        "pedidos":        []
                    }
                agrupado[cid]["pedidos"].append({
                    "pedido_id":    r["PEDIDO_ID"],
                    "fecha":        r["FECHA_CREACION"],
                    "restaurante":  r["RESTAURANTE_NOMBRE"],
                    "estado":       r["ESTADO"],
                    "estado_texto": _ESTADO_TEXTO.get(r["ESTADO"], "DESCONOCIDO"),
                    "monto":        montos.get(r["PEDIDO_ID"], 0)
                })
                agrupado[cid]["total_pedidos"] += 1

            return list(agrupado.values())

    @staticmethod
    def _map(row) -> Pedido:
        return Pedido(
            id=row["ID"],
            cliente_id=row["CLIENTE_ID"],
            restaurante_id=row["RESTAURANTE_ID"],
            repartidor_id=row["REPARTIDOR_ID"],
            fecha_creacion=row["FECHA_CREACION"],
            fecha_entrega=row["FECHA_ENTREGA"],
            estado=row["ESTADO"],
            distancia_km=row["DISTANCIA_KM"]
        )

