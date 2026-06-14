class DetallePedido:
    def __init__(
        self,
        id:              int   = None,
        pedido_id:       int   = None,
        combo_id:        int   = None,
        cantidad:        int   = 1,
        precio_unitario: float = 0.0,
        configuracion:   str   = "{}"
    ):
        self.id              = id
        self.pedido_id       = pedido_id
        self.combo_id        = combo_id
        self.cantidad        = cantidad
        self.precio_unitario = precio_unitario
        self.configuracion   = configuracion