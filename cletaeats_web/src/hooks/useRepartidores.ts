import { createEntityHooks } from '../lib/react-query/createEntityHooks'
import { repartidoresService } from '../services/repartidores/repartidoresService'
import { queryKeys } from '../lib/react-query/queryKeys'
import { notificationService } from '../services/ui/notificationService'
import { confirmService } from '../services/ui/confirmService'
import type { Repartidor, RepartidorRequest } from '../types/repartidores'

const hooks = createEntityHooks<Repartidor, RepartidorRequest>(
    queryKeys.repartidores,
    repartidoresService,
)

export const useRepartidores = () => {
    const query     = hooks.useList()
    const createMut = hooks.useCreate()
    const updateMut = hooks.useUpdate()
    const removeMut = hooks.useRemove()

    const handleSubmit = async (data: RepartidorRequest, repartidor?: Repartidor | null) => {
        try {
            if (repartidor) {
                await updateMut.mutateAsync({ id: repartidor.id, body: data })
                notificationService.success('Repartidor actualizado')
            } else {
                await createMut.mutateAsync(data)
                notificationService.success('Repartidor creado')
            }
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error guardando repartidor')
            throw e
        }
    }

    const handleToggleEstado = async (repartidor: Repartidor) => {
        const accion = repartidor.estado === 1 ? 'desactivar' : 'activar'
        const ok = await confirmService.confirm(
            `¿Querés ${accion} a "${repartidor.nombre}"?`,
        )
        if (!ok) return
        try {
            await updateMut.mutateAsync({
                id: repartidor.id,
                body: { estado: repartidor.estado === 1 ? 0 : 1 } as any,
            })
            notificationService.success(`Repartidor ${accion === 'activar' ? 'activado' : 'desactivado'}`)
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error cambiando estado')
        }
    }

    const handleDelete = async (repartidor: Repartidor) => {
        const ok = await confirmService.delete(repartidor.nombre)
        if (!ok) return
        try {
            await removeMut.mutateAsync(repartidor.id)
            notificationService.success('Repartidor eliminado')
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error eliminando repartidor')
        }
    }

    return {
        repartidores: query.data ?? [],
        loading:      query.isLoading,
        error:        query.error,
        handleSubmit,
        handleToggleEstado,
        handleDelete,
        creating:     createMut.isPending,
        updating:     updateMut.isPending,
    }
}