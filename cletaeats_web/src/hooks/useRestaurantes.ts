import { createEntityHooks } from '../lib/react-query/createEntityHooks'
import { restaurantesService } from '../services/restaurantes/restaurantesService'
import { queryKeys } from '../lib/react-query/queryKeys'
import { notificationService } from '../services/ui/notificationService'
import { confirmService } from '../services/ui/confirmService'
import type { Restaurante, RestauranteRequest } from '../types/restaurantes'

const hooks = createEntityHooks<Restaurante, RestauranteRequest>(
    queryKeys.restaurantes,
    restaurantesService,
)

export const useRestaurantes = () => {
    const query     = hooks.useList()
    const createMut = hooks.useCreate()
    const updateMut = hooks.useUpdate()
    const removeMut = hooks.useRemove()

    const handleSubmit = async (data: RestauranteRequest, restaurante?: Restaurante | null) => {
        try {
            if (restaurante) {
                await updateMut.mutateAsync({ id: restaurante.id, body: data })
                notificationService.success('Restaurante actualizado')
            } else {
                await createMut.mutateAsync(data)
                notificationService.success('Restaurante creado')
            }
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error guardando restaurante')
            throw e
        }
    }

    const handleToggleEstado = async (restaurante: Restaurante) => {
        const accion = restaurante.estado === 1 ? 'desactivar' : 'activar'
        const ok = await confirmService.confirm(
            `¿Querés ${accion} "${restaurante.nombre}"?`,
        )
        if (!ok) return
        try {
            await updateMut.mutateAsync({
                id: restaurante.id,
                body: { estado: restaurante.estado === 1 ? 0 : 1 } as any,
            })
            notificationService.success(`Restaurante ${accion === 'activar' ? 'activado' : 'desactivado'}`)
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error cambiando estado')
        }
    }

    const handleDelete = async (restaurante: Restaurante) => {
        const ok = await confirmService.delete(restaurante.nombre)
        if (!ok) return
        try {
            await removeMut.mutateAsync(restaurante.id)
            notificationService.success('Restaurante eliminado')
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error eliminando restaurante')
        }
    }

    return {
        restaurantes: query.data ?? [],
        loading:      query.isLoading,
        error:        query.error,
        handleSubmit,
        handleToggleEstado,
        handleDelete,
        creating:     createMut.isPending,
        updating:     updateMut.isPending,
    }
}