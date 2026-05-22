import { useState, useMemo } from 'react'
import Button from '../../components/common/buttons/Button'
import DataTable from '../../components/common/table/DataTable'
import Modal from '../../components/common/modal/Modal'
import StatusBadge from '../../components/common/table/StatusBadge'
import { useProductos } from '../../hooks/useProductos'
import { useRestaurantes } from '../../hooks/useRestaurantes'
import TextInput from '../../components/common/inputs/TextInput'
import type { Producto, ProductoRequest } from '../../types/productos'

const ProductosTab = () => {
    const { productos, loading, handleSubmit, handleDelete } = useProductos()
    const { restaurantes } = useRestaurantes()
    const [isOpen, setIsOpen]       = useState(false)
    const [selected, setSelected]   = useState<Producto | null>(null)
    const [form, setForm]           = useState<ProductoRequest>({ restaurante_id: 0, nombre: '', descripcion: '' })
    const [saving, setSaving]       = useState(false)
    const [filtroRestaurante, setFiltroRestaurante] = useState<number>(0)

    const productosFiltrados = useMemo(() => {
        if (filtroRestaurante === 0) return productos
        return productos.filter(p => p.restaurante_id === filtroRestaurante)
    }, [productos, filtroRestaurante])

    const openCreate = () => { setSelected(null); setForm({ restaurante_id: filtroRestaurante || 0, nombre: '', descripcion: '' }); setIsOpen(true) }
    const openEdit   = (p: Producto) => { setSelected(p); setForm({ restaurante_id: p.restaurante_id, nombre: p.nombre, descripcion: p.descripcion }); setIsOpen(true) }

    const onSave = async () => {
        setSaving(true)
        try { await handleSubmit(form, selected); setIsOpen(false) }
        catch (error) { console.error(error)} finally { setSaving(false) }
    }

    return (
        <div>
            {/* ── Filtro por restaurante ── */}
            <div>
                <div className="filter-bar">
                    <label>Filtrar por:</label>
                    <select
                        className="filter-select"
                        value={filtroRestaurante}
                        onChange={e => setFiltroRestaurante(Number(e.target.value))}
                    >
                        <option value={0}>Todos los restaurantes</option>
                        {restaurantes.map(r => (
                            <option key={r.id} value={r.id}>{r.nombre}</option>
                        ))}
                    </select>
                    {filtroRestaurante !== 0 && (
                        <button className="filter-clear-btn" onClick={() => setFiltroRestaurante(0)} title="Limpiar">×</button>
                    )}
                    <Button onClick={openCreate}>+ Nuevo Producto</Button>
                </div>
                {filtroRestaurante !== 0 && (
                    <p className="filter-result-text">
                        {productosFiltrados.length} producto(s) en {restaurantes.find(r => r.id === filtroRestaurante)?.nombre}
                    </p>
                )}
                <Button onClick={openCreate}>+ Nuevo Producto</Button>
            </div>

            {filtroRestaurante !== 0 && (
                <p>
                    {productosFiltrados.length} producto(s) en {restaurantes.find(r => r.id === filtroRestaurante)?.nombre}
                </p>
            )}

            <DataTable
                loading={loading}
                data={productosFiltrados}
                columns={[
                    { key: 'nombre',            title: 'Nombre' },
                    { key: 'restaurante_nombre', title: 'Restaurante' },
                    { key: 'descripcion',        title: 'Descripción' },
                    { key: 'estado', title: 'Estado', render: (p: Producto) => <StatusBadge status={p.estado === 1 ? 'ACTIVO' : 'INACTIVO'} /> },
                    {
                        key: 'actions', title: 'Acciones',
                        render: (p: Producto) => (
                            <div className="flex gap-2">
                                <Button variant="secondary" onClick={() => openEdit(p)}>Editar</Button>
                                <Button variant="danger" onClick={() => handleDelete(p)}>Eliminar</Button>
                            </div>
                        ),
                    },
                ]}
            />

            <Modal isOpen={isOpen} title={selected ? 'Editar Producto' : 'Nuevo Producto'} onClose={() => setIsOpen(false)}>
                <div className="flex flex-col gap-4">
                    <div className="text-input-container">
                        <label className="text-input-label">Restaurante *</label>
                        <select
                            className="text-input"
                            value={form.restaurante_id}
                            onChange={e => setForm(p => ({ ...p, restaurante_id: Number(e.target.value) }))}
                        >
                            <option value={0} disabled>Seleccioná un restaurante</option>
                            {restaurantes.map(r => <option key={r.id} value={r.id}>{r.nombre}</option>)}
                        </select>
                    </div>
                    <TextInput label="Nombre" value={form.nombre} onChange={v => setForm(p => ({ ...p, nombre: v }))} rules={[{ type: 'required' }]} />
                    <TextInput label="Descripción" value={form.descripcion} onChange={v => setForm(p => ({ ...p, descripcion: v }))} hint="Opcional" />
                    <div className="flex justify-end gap-3 pt-3">
                        <Button variant="secondary" onClick={() => setIsOpen(false)}>Cancelar</Button>
                        <Button onClick={onSave} disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button>
                    </div>
                </div>
            </Modal>
        </div>
    )
}

export default ProductosTab
