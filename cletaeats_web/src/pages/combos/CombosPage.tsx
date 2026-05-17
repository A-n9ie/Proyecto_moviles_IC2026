import { useState } from 'react'
import Button from '../../components/common/buttons/Button'
import DataTable from '../../components/common/table/DataTable'
import Modal from '../../components/common/modal/Modal'
import StatusBadge from '../../components/common/table/StatusBadge'
import PageHeader from '../../components/ui/pageHeader/PageHeader'
import ComboForm from './ComboForm'
import { useCombos } from '../../hooks/useCombos'
import { formatColones } from '../../utils'
import type { Combo } from '../../types/combos'

const CombosPage = () => {
    const { combos, loading, handleSubmit, handleToggleEstado, handleDelete } = useCombos()
    const [isOpen, setIsOpen]     = useState(false)
    const [selected, setSelected] = useState<Combo | null>(null)

    const handleCreate = () => { setSelected(null); setIsOpen(true) }
    const handleEdit   = (c: Combo) => { setSelected(c); setIsOpen(true) }

    return (
        <div>
            <PageHeader
                title="Combos"
                subtitle="Menú de combos por restaurante"
                action={<Button onClick={handleCreate}>+ Nuevo Combo</Button>}
            />
            <DataTable
                loading={loading}
                data={combos}
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