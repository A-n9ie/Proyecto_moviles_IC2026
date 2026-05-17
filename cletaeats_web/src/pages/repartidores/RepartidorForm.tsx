import {
    useState,
} from 'react'

import Button from '../../components/common/buttons/Button'

import TextInput from '../../components/common/inputs/TextInput'

import type {
    Repartidor,
    RepartidorRequest,
} from '../../types/repartidores'

interface Props {
    repartidor?: Repartidor | null

    onSubmit: (
        body: RepartidorRequest,
    ) => Promise<void>

    onClose: () => void
}

const RepartidorForm = ({
                            repartidor,
                            onSubmit,
                            onClose,
                        }: Props) => {

    const [form, setForm] =
        useState<RepartidorRequest>({
            nombre: repartidor?.nombre ?? '',
            email: repartidor?.correo ?? repartidor?.email ?? '',
            cedula: repartidor?.cedula ?? '',
            telefono: repartidor?.telefono ?? '',
            direccion: '',
        })

    const [loading, setLoading] =
        useState(false)

    const handleSubmit =
        async () => {
            try {
                setLoading(true)

                await onSubmit(form)

                onClose()
            } catch (error) {
                console.error(error)
            } finally {
                setLoading(false)
            }
        }

    return (
        <div className="flex flex-col gap-4">
            <TextInput label="Cédula" value={form.cedula}
                       onChange={(v) => setForm(p => ({...p, cedula: v}))}
                       rules={[{ type: 'required' }, { type: 'pattern', value: /^\d-\d{4}-\d{4}$/, message: 'Formato: 1-2345-6789' }]}
            />

            <TextInput
                label="Nombre"
                value={form.nombre}
                onChange={(value) =>
                    setForm((prev) => ({
                        ...prev,

                        nombre: value,
                    }))
                }
            />

            <TextInput
                label="Correo"
                type="email"
                value={form.email}
                onChange={(value) =>
                    setForm((prev) => ({
                        ...prev,

                        email: value,
                    }))
                }
            />

            <TextInput
                label="Teléfono"
                value={form.telefono}
                onChange={(value) =>
                    setForm((prev) => ({
                        ...prev,

                        telefono: value,
                    }))
                }
            />

            <TextInput label="Dirección" value={form.direccion}
                       onChange={(v) => setForm(p => ({...p, direccion: v}))}
                       rules={[{ type: 'required' }]}
            />

            <div className="flex justify-end gap-3 pt-4">
                <Button
                    variant="secondary"
                    onClick={onClose}
                >
                    Cancelar
                </Button>

                <Button
                    onClick={
                        handleSubmit
                    }
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

export default RepartidorForm