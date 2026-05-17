export interface Pedido {
    id: number
    cliente_nombre: string
    restaurante_nombre: string
    distancia_km: number
    estado: number
    estado_texto: string
    items_count: number
    fecha_creacion: string
    fecha_entrega: string | null
}

export interface PedidoFilters {
    search: string
    estado: string
}

export interface PedidoColumn {
    key: string
    title: string
    render?: (pedido: Pedido) => React.ReactNode
}