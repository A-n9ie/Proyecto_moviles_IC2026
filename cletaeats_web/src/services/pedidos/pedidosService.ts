import { axiosClient } from '../api/axiosClient'
import type { Pedido } from '../../types/pedidos'

export const pedidosService = {
    getAll: async (): Promise<Pedido[]> => {
        const r = await axiosClient.get<Pedido[]>('/admin/pedidos')
        return r.data
    },
}