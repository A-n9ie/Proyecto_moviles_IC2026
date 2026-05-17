export interface DashboardStats {
    totalPedidos: number
    pedidosActivos: number
    totalClientes: number
    repartidoresActivos: number
}

export interface PedidoReciente {
    id: number
    cliente: string
    restaurante: string
    total: number
    estado: string
}

export interface RepartidorEstado {
    id: number
    nombre: string
    estado: string
}

export interface DashboardResponse {
    stats: DashboardStats

    pedidosRecientes: PedidoReciente[]

    repartidores: RepartidorEstado[]
}