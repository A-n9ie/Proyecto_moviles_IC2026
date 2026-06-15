import { useState } from 'react'
import Button from '../../components/common/buttons/Button'
import TextInput from '../../components/common/inputs/TextInput'
import { useRestaurantes } from '../../hooks/useRestaurantes'
import { useProductos } from '../../hooks/useProductos'
import type { Combo, ComboRequest } from '../../types/combos'
import { combosService } from '../../services/combos/combosService'

interface Props {
    combo?: Combo | null
    onSubmit: (data: ComboRequest) => Promise<void>
    onClose: () => void
}

const ComboForm = ({ combo, onSubmit, onClose }: Props) => {
    const { restaurantes } = useRestaurantes()

    const [form, setForm] = useState<ComboRequest>({
        restaurante_id: combo?.restaurante_id ?? 0,
        numero_combo: combo?.numero_combo ?? 1,
        nombre: combo?.nombre ?? '',
        descripcion: combo?.descripcion ?? '',
        precio: combo?.precio ?? 0,
        producto_ids: combo?.productos?.map(p => p.id) ?? [],
        imagen_url: combo?.imagen_url ?? '',  // ← nuevo
    })

    const { productos } = useProductos()
    const productosDelRestaurante = productos.filter(p => p.restaurante_id === form.restaurante_id && p.estado === 1)

    const [loading, setLoading] = useState(false)
    const [uploadingImg, setUploadingImg] = useState(false)

    const handleSubmit = async () => {
        try {
            setLoading(true)
            await onSubmit(form)
            onClose()
        } catch {
            // hook muestra notificación
        } finally {
            setLoading(false)
        }
    }

    const set = (key: keyof ComboRequest) => (v: string) =>
        setForm(p => ({ ...p, [key]: key === 'precio' || key === 'numero_combo' || key === 'restaurante_id' ? Number(v) : v }))

    return (
        <div className="flex flex-col gap-4">
            <div className="text-input-container">
                <label className="text-input-label">
                    Restaurante <span className="text-input-required">*</span>
                </label>
                <select
                    className="text-input"
                    value={form.restaurante_id}
                    onChange={(e) => setForm(p => ({...p, restaurante_id: Number(e.target.value)}))}
                    style={{ background: 'var(--gris-oscuro)', color: 'var(--blanco)', border: '1px solid var(--gris-borde)', borderRadius: 8, padding: '9px 12px' }}
                >
                    <option value={0} disabled>Seleccioná un restaurante</option>
                    {restaurantes.map(r => (
                        <option key={r.id} value={r.id}>{r.nombre}</option>
                    ))}
                </select>
            </div>

            <TextInput
                label="Número de combo"
                type="number"
                value={String(form.numero_combo)}
                onChange={set('numero_combo')}
                rules={[{ type: 'required' }, { type: 'min', value: 1 }]}
            />

            <TextInput
                label="Nombre"
                value={form.nombre}
                onChange={set('nombre')}
                rules={[{ type: 'required' }, { type: 'minLength', value: 3 }]}
            />

            <TextInput
                label="Descripción"
                value={form.descripcion}
                onChange={set('descripcion')}
                hint="Opcional — ingredientes o características del combo"
            />

            <div className="text-input-container">
                <label className="text-input-label">Productos del combo</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, maxHeight: 180, overflowY: 'auto' }}>
                    {productosDelRestaurante.length === 0
                        ? <span style={{ color: 'var(--gris-texto)', fontSize: 13 }}>
                            {form.restaurante_id === 0 ? 'Seleccioná un restaurante primero' : 'Sin productos para este restaurante'}
                        </span>
                        : productosDelRestaurante.map(prod => (
                            <label key={prod.id} style={{ display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer' }}>
                                <input
                                    type="checkbox"
                                    checked={form.producto_ids.includes(prod.id)}
                                    onChange={e => {
                                        setForm(p => ({
                                            ...p,
                                            producto_ids: e.target.checked
                                                ? [...p.producto_ids, prod.id]
                                                : p.producto_ids.filter(id => id !== prod.id)
                                        }))
                                    }}
                                />
                                {prod.nombre}
                            </label>
                        ))
                    }
                </div>
            </div>

            <TextInput
                label="Precio (₡)"
                type="number"
                value={String(form.precio)}
                onChange={set('precio')}
                rules={[{ type: 'required' }, { type: 'min', value: 1, message: 'El precio debe ser mayor a 0' }]}
            />

            <div className="text-input-container">
    <label className="text-input-label">Imagen del combo</label>
    <input
        type="file"
        accept="image/*"
        disabled={uploadingImg}
        onChange={async (e) => {
            const file = e.target.files?.[0]
            if (!file) return
            setUploadingImg(true)
            try {
                const url = await combosService.uploadImagen(file)
                setForm(p => ({ ...p, imagen_url: url }))
            } finally {
                setUploadingImg(false)
            }
        }}
        style={{ color: 'var(--blanco)' }}
    />
    {uploadingImg && <span style={{ color: 'var(--gris-texto)', fontSize: 13 }}>Subiendo imagen...</span>}
    {form.imagen_url && (
        <img
            src={`https://proyecto-moviles-ic2026.onrender.com${form.imagen_url}`}
            alt="preview"
            style={{ marginTop: 8, height: 120, width: '100%', borderRadius: 8, objectFit: 'cover' }}
        />
    )}
</div>

            <div className="flex justify-end gap-3 pt-3">
                <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                <Button onClick={handleSubmit} disabled={loading}>
                    {loading ? 'Guardando...' : 'Guardar'}
                </Button>
            </div>
        </div>
    )
}

export default ComboForm