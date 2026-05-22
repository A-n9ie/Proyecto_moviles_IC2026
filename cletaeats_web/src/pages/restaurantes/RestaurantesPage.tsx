import { useState, useMemo } from 'react'
import Button from '../../components/common/buttons/Button'
import Modal from '../../components/common/modal/Modal'
import Tabs from '../../components/common/tabs/Tabs'
import DataTable from '../../components/common/table/DataTable'
import PageHeader from '../../components/ui/pageHeader/PageHeader'
import RestauranteForm from './RestauranteForm'
import CategoriasTab from './CategoriasTab'
import ProductosTab from './ProductosTab'
import { useRestaurantes } from '../../hooks/useRestaurantes'
import { useCategorias } from '../../hooks/useCategorias'
import type { Restaurante } from '../../types/restaurantes'
import StatusBadge from "../../components/common/table/StatusBadge.tsx"

const RestaurantesPage = () => {
    const { restaurantes, loading, handleSubmit, handleToggleEstado, handleDelete } = useRestaurantes()
    const { categorias } = useCategorias()

    const [activeTab, setActiveTab]               = useState('restaurantes')
    const [isModalOpen, setIsModalOpen]           = useState(false)
    const [selectedRestaurante, setSelectedRestaurante] = useState<Restaurante | null>(null)
    const [filtroCategoria, setFiltroCategoria]   = useState<number>(0)

    // Filtro por categoría en la tabla de restaurantes
    const restaurantesFiltrados = useMemo(() => {
        if (filtroCategoria === 0) return restaurantes
        return restaurantes.filter(r =>
            r.categorias?.some(c => c.id === filtroCategoria)
        )
    }, [restaurantes, filtroCategoria])

    const handleCreate = () => { setSelectedRestaurante(null); setIsModalOpen(true) }
    const handleEdit   = (r: Restaurante) => { setSelectedRestaurante(r); setIsModalOpen(true) }

    return (
        <div>
            <PageHeader
                title="Restaurantes"
                subtitle="Administración de restaurantes y menús"
                action={<Button onClick={handleCreate}>+ Nuevo Restaurante</Button>}
            />

            <Tabs
                activeTab={activeTab}
                onChange={setActiveTab}
                tabs={[
                    { key: 'restaurantes', label: 'Restaurantes' },
                    { key: 'categorias',   label: 'Categorías'   },
                    { key: 'productos',    label: 'Productos'    },
                ]}
            />

            {activeTab === 'restaurantes' && (
                <>
                    {/* ── Filtro por categoría ── */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, margin: '16px 0', maxWidth: 380 }}>
                        <label style={{ color: 'var(--texto-secundario)', fontSize: 13, whiteSpace: 'nowrap' }}>
                            Categoría:
                        </label>
                        <select
                            value={filtroCategoria}
                            onChange={e => setFiltroCategoria(Number(e.target.value))}
                            style={{
                                background: 'var(--gris-oscuro)', color: 'var(--blanco)',
                                border: '1px solid var(--gris-borde)', borderRadius: 8,
                                padding: '8px 12px', fontSize: 13, flex: 1
                            }}
                        >
                            <option value={0}>Todas las categorías</option>
                            {categorias.map((c: { id: number; nombre: string }) => (
                                <option key={c.id} value={c.id}>{c.nombre}</option>
                            ))}
                        </select>
                        {filtroCategoria !== 0 && (
                            <button
                                onClick={() => setFiltroCategoria(0)}
                                style={{
                                    background: 'none', border: 'none', color: 'var(--naranja)',
                                    cursor: 'pointer', fontSize: 18, lineHeight: 1, padding: '4px 6px'
                                }}
                                title="Limpiar filtro"
                            >×</button>
                        )}
                    </div>

                    {filtroCategoria !== 0 && (
                        <p style={{ color: 'var(--texto-secundario)', fontSize: 12, marginBottom: 8 }}>
                            {restaurantesFiltrados.length} restaurante(s) con categoría "{categorias.find((c: any) => c.id === filtroCategoria)?.nombre}"
                        </p>
                    )}

                    <DataTable
                        loading={loading}
                        data={restaurantesFiltrados}
                        columns={[
                            { key: 'nombre', title: 'Nombre' },
                            {
                                key: 'categorias', title: 'Categorías',
                                render: (r: Restaurante) => r.categorias?.map(c => c.nombre).join(', ') ?? '-',
                            },
                            { key: 'direccion', title: 'Dirección' },
                            {
                                key: 'estado', title: 'Estado',
                                render: (r: Restaurante) => <StatusBadge status={r.estado === 1 ? 'ACTIVO' : 'INACTIVO'} />,
                            },
                            {
                                key: 'actions', title: 'Acciones',
                                render: (r: Restaurante) => (
                                    <div className="flex gap-2">
                                        <Button variant="secondary" onClick={() => handleEdit(r)}>Editar</Button>
                                        <Button
                                            variant={r.estado === 1 ? 'danger' : 'success'}
                                            onClick={() => handleToggleEstado(r)}
                                        >
                                            {r.estado === 1 ? 'Desactivar' : 'Activar'}
                                        </Button>
                                        <Button variant="danger" onClick={() => handleDelete(r)}>Eliminar</Button>
                                    </div>
                                ),
                            },
                        ]}
                    />
                </>
            )}

            {activeTab === 'categorias' && <CategoriasTab />}
            {activeTab === 'productos'  && <ProductosTab />}

            <Modal
                isOpen={isModalOpen}
                title={selectedRestaurante ? 'Editar Restaurante' : 'Nuevo Restaurante'}
                onClose={() => setIsModalOpen(false)}
            >
                <RestauranteForm
                    key={selectedRestaurante?.id ?? 'new'}
                    restaurante={selectedRestaurante}
                    onClose={() => setIsModalOpen(false)}
                    onSubmit={async (body) => handleSubmit(body, selectedRestaurante)}
                />
            </Modal>
        </div>
    )
}

export default RestaurantesPage
