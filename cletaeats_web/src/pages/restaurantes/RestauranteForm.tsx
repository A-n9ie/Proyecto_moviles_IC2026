import {
    useState,
} from 'react'

import Button from '../../components/common/buttons/Button'

import TextInput from '../../components/common/inputs/TextInput'

import { useCategorias } from '../../hooks/useCategorias'

import MapaPicker from './MapaPicker'
import ImageUpload from '../../components/common/upload/ImageUpload'

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
    const [form, setForm] = useState<RestauranteRequest>({
    nombre: restaurante?.nombre ?? '',
    categoria_ids: restaurante?.categorias?.map(c => c.id) ?? [],
    direccion: restaurante?.direccion ?? '',
    imagen_url: restaurante?.imagen_url ?? '',
    cedula_juridica: restaurante?.cedula_juridica ?? '',
    latitud: restaurante?.latitud ?? null,
    longitud: restaurante?.longitud ?? null,
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

    const { categorias } = useCategorias()

    return (
        <div className="flex flex-col ">
            <TextInput
                label="Cédula Jurídica"
                value={form.cedula_juridica}
                onChange={(v) =>
                    setForm(p => ({
                        ...p,
                        cedula_juridica: v
                    }))
                }
                rules={[
                    { type: 'required' }
                ]}
            />
            <TextInput label="Nombre" value={form.nombre}
                       onChange={(v) => setForm(p => ({...p, nombre: v}))}
                       rules={[{ type: 'required' }, { type: 'minLength', value: 3 }]}
            />
            <div className="text-input-container">
                <label className="text-input-label">Categorías *</label>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {categorias.map(cat => (
                        <label key={cat.id} style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}>
                            <input
                                type="checkbox"
                                checked={form.categoria_ids.includes(cat.id)}
                                onChange={(e) => {
                                    setForm(p => ({
                                        ...p,
                                        categoria_ids: e.target.checked
                                            ? [...p.categoria_ids, cat.id]
                                            : p.categoria_ids.filter(id => id !== cat.id)
                                    }))
                                }}
                            />
                            {cat.nombre}
                        </label>
                    ))}
                </div>
            </div>
            <TextInput label="Dirección" value={form.direccion}
                       onChange={(v) => setForm(p => ({...p, direccion: v}))}
                       rules={[{ type: 'required' }]}
            />

            <MapaPicker
                latitud={form.latitud}
                longitud={form.longitud}
                onChange={(lat, lng) => setForm(p => ({ ...p, latitud: lat, longitud: lng }))}
            />

            <ImageUpload
    value={form.imagen_url}
    onChange={(url) => setForm(p => ({ ...p, imagen_url: url }))}
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

export default RestauranteForm