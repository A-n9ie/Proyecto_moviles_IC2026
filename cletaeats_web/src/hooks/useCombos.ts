import { createEntityHooks } from '../lib/react-query/createEntityHooks'
import { combosService } from '../services/combos/combosService'
import { queryKeys } from '../lib/react-query/queryKeys'
import { notificationService } from '../services/ui/notificationService'
import { confirmService } from '../services/ui/confirmService'
import type { Combo, ComboRequest } from '../types/combos'

const hooks = createEntityHooks<Combo, ComboRequest>(
    queryKeys.combos,
    combosService,
)

export const useCombos = () => {
    const query     = hooks.useList()
    const createMut = hooks.useCreate()
    const updateMut = hooks.useUpdate()
    const removeMut = hooks.useRemove()

    const handleSubmit = async (data: ComboRequest, combo?: Combo | null) => {
        try {
            if (combo) {
                await updateMut.mutateAsync({ id: combo.id, body: data })
                notificationService.success('Combo actualizado')
            } else {
                await createMut.mutateAsync(data)
                notificationService.success('Combo creado')
            }
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error guardando combo')
            throw e
        }
    }

    const handleToggleEstado = async (combo: Combo) => {
        const accion = combo.estado === 1 ? 'desactivar' : 'activar'
        const ok = await confirmService.confirm(`¿Querés ${accion} "${combo.nombre}"?`)
        if (!ok) return
        try {
            await updateMut.mutateAsync({
                id: combo.id,
                body: { estado: combo.estado === 1 ? 0 : 1 } as any,
            })
            notificationService.success(`Combo ${accion === 'activar' ? 'activado' : 'desactivado'}`)
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error cambiando estado')
        }
    }

    const handleDelete = async (combo: Combo) => {
        const ok = await confirmService.delete(combo.nombre)
        if (!ok) return
        try {
            await removeMut.mutateAsync(combo.id)
            notificationService.success('Combo eliminado')
        } catch (e: any) {
            notificationService.error(e?.message ?? 'Error eliminando combo')
        }
    }

    return {
        combos:   query.data ?? [],
        loading:  query.isLoading,
        error:    query.error,
        handleSubmit,
        handleToggleEstado,
        handleDelete,
        creating: createMut.isPending,
        updating: updateMut.isPending,
    }
}