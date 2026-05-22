# core/entities/tarjeta_cliente.py

class TarjetaCliente:
    def __init__(
        self,
        id: int = None,
        cliente_id: int = None,
        numero: str = "",
        alias: str = "",
        es_principal: int = 0
    ):
        self.id          = id
        self.cliente_id  = cliente_id
        self.numero      = numero
        self.alias       = alias
        self.es_principal = es_principal

    def __str__(self):
        return f"TarjetaCliente(id={self.id}, cliente_id={self.cliente_id}, alias={self.alias})"