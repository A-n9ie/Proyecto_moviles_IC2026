// src/services/ui/confirmService.ts — reemplazar completo
import Swal from 'sweetalert2'

export const confirmService = {
    delete: async (entityName: string) => {
        const result = await Swal.fire({
            title: '¿Estás seguro?',
            text: `Vas a eliminar "${entityName}". Esta acción no se puede deshacer.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#E85D04',
        })
        return result.isConfirmed
    },

    confirm: async (message: string, title = '¿Confirmar acción?') => {
        const result = await Swal.fire({
            title,
            text: message,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Confirmar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#E85D04',
        })
        return result.isConfirmed
    },
}