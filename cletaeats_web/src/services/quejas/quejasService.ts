import { axiosClient } from '../api/axiosClient'
import type { Queja, AccionQueja } from '../../types/quejas'

export const quejasService = {
    getAll: async (estado?: number): Promise<Queja[]> => {
        const params = estado !== undefined ? { estado } : {}
        const r = await axiosClient.get<Queja[]>('/admin/quejas', { params })
        return r.data
    },
    clasificar: async (id: number, accion: AccionQueja): Promise<void> => {
        await axiosClient.patch(`/admin/quejas/${id}`, { accion })
    },
}