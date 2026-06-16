import { axiosClient } from '../api/axiosClient'
import type { DashboardResponse } from '../../types/dashboard'

export const dashboardService = {
    getDashboardData: async (): Promise<DashboardResponse> => {
        const [pedidosR, clientesR, repartidoresR] = await Promise.all([
            axiosClient.get('/admin/pedidos'),
            axiosClient.get('/admin/clientes'),
            axiosClient.get('/admin/repartidores'),
        ])
        const pedidos     = pedidosR.data     as any[]
        const clientes    = clientesR.data    as any[]
        const repartidores = repartidoresR.data as any[]

        const ESTADOS_ACTIVOS = [0, 1, 2]   // CREADO, EN_PREPARACION, EN_CAMINO
        return {
            stats: {
                totalPedidos:        pedidos.length,
                pedidosActivos:      pedidos.filter(p => ESTADOS_ACTIVOS.includes(p.estado)).length,
                totalClientes:       clientes.length,
                repartidoresActivos: repartidores.filter(r => r.estado === 1).length,
            },
            pedidosRecientes: pedidos.slice(0, 5).map(p => ({
                id:          p.id,
                cliente:     p.cliente_nombre ?? '—',
                restaurante: p.restaurante_nombre ?? '—',
                total:       p.total ?? 0,
                estado:      p.estado_texto ?? String(p.estado),
            })),
            repartidores: repartidores.map(r => ({
                id:     r.id,
                nombre: r.nombre,
                estado: r.estado === 1 ? 'DISPONIBLE' : 'OCUPADO',
            })),
        }
    },
}