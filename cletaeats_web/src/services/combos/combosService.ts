import { axiosClient } from '../api/axiosClient'
import type { Combo, ComboRequest } from '../../types/combos'

export const combosService = {
    getAll: async (): Promise<Combo[]> => {
        const r = await axiosClient.get<Combo[]>('/admin/combos')
        return r.data
    },
    create: async (body: ComboRequest): Promise<Combo> => {
        const r = await axiosClient.post<Combo>('/admin/combos', body)
        return r.data
    },
    update: async (id: number, body: Partial<ComboRequest>): Promise<Combo> => {
        const r = await axiosClient.put<Combo>(`/admin/combos/${id}`, body)
        return r.data
    },
    remove: async (id: number): Promise<void> => {
        await axiosClient.delete(`/admin/combos/${id}`)
    },
}