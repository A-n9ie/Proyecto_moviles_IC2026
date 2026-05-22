import { useState, useMemo } from 'react'
import Button from '../../components/common/buttons/Button'
import DataTable from '../../components/common/table/DataTable'
import Modal from '../../components/common/modal/Modal'
import StatusBadge from '../../components/common/table/StatusBadge'
import PageHeader from '../../components/ui/pageHeader/PageHeader'
import ComboForm from './ComboForm'
import { useCombos } from '../../hooks/useCombos'
import { useRestaurantes } from '../../hooks/useRestaurantes'
import { formatColones } from '../../utils'
import type { Combo } from '../../types/combos'

const CombosPage = () => {
    const { combos, loading, handleSubmit, handleToggleEstado, handleDelete } = useCombos()
    const { restaurantes } = useRestaurantes()
    const [isOpen, setIsOpen]     = useState(false)
    const [selected, setSelected] = useState<Combo | null>(null)
    const [filtroRestaurante, setFiltroRestaurante] = useState<number>(0)

    const combosFiltrados = useMemo(() => {
        if (filtroRestaurante === 0) return combos
        return combos.filter(c => c.restaurante_id === filtroRestaurante)
    }, [combos, filtroRestaurante])

    const handleCreate = () => { setSelected(null); setIsOpen(true) }
    const handleEdit   = (c: Combo) => { setSelected(c); setIsOpen(true) }

    return (
        <div>
            <PageHeader
                title="Combos"
                subtitle="Menú de combos por restaurante"
                action={<Button onClick={handleCreate}>+ Nuevo Combo</Button>}
            />

            {/* ── Filtro por restaurante ── */}
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
                    <button className="filter-clear-btn" onClick={() => setFiltroRestaurante(0)}>×</button>
                )}
            </div>

            {filtroRestaurante !== 0 && (
                <p>
                    {combosFiltrados.length} combo(s) en {restaurantes.find(r => r.id === filtroRestaurante)?.nombre}
                </p>
            )}

            <DataTable
                loading={loading}
                data={combosFiltrados}
                columns={[
                    { key: 'numero_combo',      title: '#' },
                    { key: 'nombre',             title: 'Nombre' },
                    { key: 'restaurante_nombre', title: 'Restaurante' },
                    {
                        key: 'precio', title: 'Precio',
                        render: (c: Combo) => formatColones(c.precio),
                    },
                    {
                        key: 'estado', title: 'Estado',
                        render: (c: Combo) => (
                            <StatusBadge status={c.estado === 1 ? 'ACTIVO' : 'INACTIVO'} />
                        ),
                    },
                    {
                        key: 'actions', title: 'Acciones',
                        render: (c: Combo) => (
                            <div className="flex gap-2">
                                <Button variant="secondary" onClick={() => handleEdit(c)}>
                                    Editar
                                </Button>
                                <Button
                                    variant={c.estado === 1 ? 'danger' : 'success'}
                                    onClick={() => handleToggleEstado(c)}
                                >
                                    {c.estado === 1 ? 'Desactivar' : 'Activar'}
                                </Button>
                                <Button variant="danger" onClick={() => handleDelete(c)}>
                                    Eliminar
                                </Button>
                            </div>
                        ),
                    },
                ]}
            />
            <Modal
                isOpen={isOpen}
                title={selected ? 'Editar Combo' : 'Nuevo Combo'}
                onClose={() => setIsOpen(false)}
            >
                <ComboForm
                    key={selected?.id ?? 'new'}
                    combo={selected}
                    onClose={() => setIsOpen(false)}
                    onSubmit={(data) => handleSubmit(data, selected)}
                />
            </Modal>
        </div>
    )
}

export default CombosPage
