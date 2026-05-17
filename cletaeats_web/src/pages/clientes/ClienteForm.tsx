import {
    useState,
} from 'react'

import Button from '../../components/common/buttons/Button'

import TextInput from '../../components/common/inputs/TextInput'

import type {
    Cliente,
    ClienteRequest,
} from '../../types/clientes'

interface Props {
    cliente?: Cliente | null

    onSubmit: (
        data: ClienteRequest,
    ) => Promise<void>

    onClose: () => void
}

const ClienteForm = ({
                         cliente,
                         onSubmit,
                         onClose,
                     }: Props) => {
    const [form, setForm] = useState<ClienteRequest>({
        nombre: cliente?.nombre ?? '',
        email: cliente?.email ?? '',
        cedula: cliente?.cedula ?? '',
        telefono: cliente?.telefono ?? '',
        direccion: cliente?.direccion ?? '',
    })

    const [loading, setLoading] =
        useState(false)

    const [error] =
        useState('')


    const handleSubmit = async () => {
        try {
            setLoading(true)
            await onSubmit(form)
            onClose()
        } catch {
            // el hook ya muestra notificación
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="flex flex-col gap-4">
            <TextInput label="Nombre" value={form.nombre}
                       onChange={(v) => setForm(p => ({...p, nombre: v}))}
                       rules={[{ type: 'required' }, { type: 'minLength', value: 3 }]}
            />

            <TextInput label="Correo" type="email" value={form.email}
                       onChange={(v) => setForm(p => ({...p, email: v}))}
                       rules={[{ type: 'required' }, { type: 'email' }]}
            />

            <TextInput label="Cédula" value={form.cedula}
                       onChange={(v) => setForm(p => ({...p, cedula: v}))}
                       rules={[{ type: 'required' }]}
            />

            <TextInput label="Teléfono" value={form.telefono}
                       onChange={(v) => setForm(p => ({...p, telefono: v}))}
                       rules={[{ type: 'required' }, { type: 'pattern', value: /^\d{4}-?\d{4}$/, message: 'Formato: 8888-8888' }]}
            />

            <TextInput label="Dirección" value={form.direccion}
                       onChange={(v) => setForm(p => ({...p, direccion: v}))}
                       rules={[{ type: 'required' }]}
            />

            {error && (
                <div className="text-sm text-[var(--error)]">
                    {error}
                </div>
            )}

            <div className="flex justify-end gap-3 pt-3">
                <Button
                    variant="secondary"
                    onClick={onClose}
                >
                    Cancelar
                </Button>

                <Button
                    onClick={handleSubmit}
                    disabled={loading}
                >
                    {loading
                        ? 'Guardando...'
                        : 'Guardar'}
                </Button>
            </div>
        </div>
    )
}

export default ClienteForm