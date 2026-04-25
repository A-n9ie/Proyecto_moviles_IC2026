# core/entities/pedido.py

class Pedido:
    """
    ESTADO:
      0 = CREADO         (registrado, sin preparar)
      1 = EN_PREPARACION
      2 = EN_CAMINO
      3 = ENTREGADO
      4 = CANCELADO
    """
    ESTADOS = {
        0: "CREADO",
        1: "EN_PREPARACION",
        2: "EN_CAMINO",
        3: "ENTREGADO",
        4: "CANCELADO"
    }

    def __init__(
        self,
        id:             int   = None,
        cliente_id:     int   = None,
        restaurante_id: int   = None,
        repartidor_id:  int   = None,
        fecha_creacion: str   = None,
        fecha_entrega:  str   = None,
        estado:         int   = 0,
        distancia_km:   float = 0.0
    ):
        self.id             = id
        self.cliente_id     = cliente_id
        self.restaurante_id = restaurante_id
        self.repartidor_id  = repartidor_id
        self.fecha_creacion = fecha_creacion
        self.fecha_entrega  = fecha_entrega
        self.estado         = estado
        self.distancia_km   = distancia_km

    def get_estado_texto(self) -> str:
        return self.ESTADOS.get(self.estado, "DESCONOCIDO")

    def __str__(self):
        return f"Pedido(id={self.id}, estado={self.get_estado_texto()})"