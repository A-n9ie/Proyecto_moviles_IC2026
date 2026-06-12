# core/entities/queja.py

class Queja:
    """
    ESTADO:
      0 = PENDIENTE   (recién creada, sin revisar por el admin)
      1 = AMONESTADA  (el admin la revisó y generó una amonestación)
      2 = MENOR       (el admin la revisó, sin amonestación)

    MOTIVO: categoría de la queja según el cliente
      (amabilidad, tiempo, presentacion, otro)
    """
    ESTADOS = {
        0: "PENDIENTE",
        1: "AMONESTADA",
        2: "MENOR"
    }

    def __init__(
        self,
        id:            int = None,
        cliente_id:    int = None,
        repartidor_id: int = None,
        pedido_id:     int = None,
        motivo:        str = None,
        descripcion:   str = None,
        fecha:         str = None,
        estado:        int = 0
    ):
        self.id            = id
        self.cliente_id    = cliente_id
        self.repartidor_id = repartidor_id
        self.pedido_id     = pedido_id
        self.motivo        = motivo
        self.descripcion   = descripcion
        self.fecha         = fecha
        self.estado        = estado

    def get_estado_texto(self) -> str:
        return self.ESTADOS.get(self.estado, "DESCONOCIDO")

    def __str__(self):
        return f"Queja(id={self.id}, repartidor={self.repartidor_id}, estado={self.get_estado_texto()})"