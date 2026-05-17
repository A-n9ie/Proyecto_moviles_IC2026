import {
    useState,
} from 'react'

import Button from '../../components/common/buttons/Button'

import TextInput from '../../components/common/inputs/TextInput'

//import ImageUpload from '../../components/common/upload/ImageUpload'

import type {
    Restaurante,
    RestauranteRequest,
} from '../../types/restaurantes'

interface Props {
    restaurante?: Restaurante | null

    onSubmit: (
        body: RestauranteRequest,
    ) => Promise<void>

    onClose: () => void
}

const RestauranteForm = ({
                             restaurante,
                             onSubmit,
                             onClose,
                         }: Props) => {
    const [form, setForm] =
        useState<RestauranteRequest>({
            nombre: restaurante?.nombre ?? '',
            tipo_comida: restaurante?.tipo_comida ?? '',
            direccion: restaurante?.direccion ?? '',
            imagen_url: restaurante?.imagen_url ?? '',
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
            <TextInput label="Nombre" value={form.nombre}
                       onChange={(v) => setForm(p => ({...p, nombre: v}))}
                       rules={[{ type: 'required' }, { type: 'minLength', value: 3 }]}
            />
            <TextInput label="Tipo de comida" value={form.tipo_comida}
                       onChange={(v) => setForm(p => ({...p, tipo_comida: v}))}
                       placeholder="rápida, italiana, china..."
                       rules={[{ type: 'required' }]}
            />
            <TextInput label="Dirección" value={form.direccion}
                       onChange={(v) => setForm(p => ({...p, direccion: v}))}
                       rules={[{ type: 'required' }]}
            />
            <TextInput label="URL de imagen" value={form.imagen_url}
                       onChange={(v) => setForm(p => ({...p, imagen_url: v}))}
                       hint="Opcional — nombre del placeholder o URL completa"
            />

            {/*<ImageUpload
                value={form.imagen}
                onChange={(value) =>
                setForm((prev) => ({
                    ...prev,

                    imagen: value,
                }))
            }
                />*/}

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

export default RestauranteForm