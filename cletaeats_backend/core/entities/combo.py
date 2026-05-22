# core/entities/combo.py

class Combo:
    """
    Combo de un restaurante. NUMERO_COMBO entre 1 y 9.
    Precio calculado: 3000 + (numero_combo * 1000)
    """
    def __init__(
        self,
        id:             int   = None,
        restaurante_id: int   = None,
        numero_combo:   int   = None,
        nombre:         str   = "",
        descripcion:    str   = "",
        precio:         float = 0.0,
        imagen_url:     str   = "",
        estado:         int   = 1,
        productos:      list  = None 
    ):
        self.id             = id
        self.restaurante_id = restaurante_id
        self.numero_combo   = numero_combo
        self.nombre         = nombre
        self.descripcion    = descripcion
        self.precio         = precio
        self.imagen_url     = imagen_url
        self.estado         = estado
        self.productos      = productos or []

    def get_nombre(self) -> str:  return self.nombre
    def set_nombre(self, v: str): self.nombre = v
    def get_precio(self) -> float: return self.precio
    def set_precio(self, v: float): self.precio = v

    def __str__(self):
        return f"Combo(id={self.id}, #{self.numero_combo}, nombre={self.nombre}, precio={self.precio})"