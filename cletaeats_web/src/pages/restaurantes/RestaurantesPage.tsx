import {
    useState,
} from 'react'

import Button from '../../components/common/buttons/Button'

import Modal from '../../components/common/modal/Modal'

import Tabs from '../../components/common/tabs/Tabs'

import DataTable from '../../components/common/table/DataTable'

import PageHeader from '../../components/ui/pageHeader/PageHeader'

import RestauranteForm from './RestauranteForm'

import { useRestaurantes } from '../../hooks/useRestaurantes'

import type {
    Restaurante,
} from '../../types/restaurantes'
import StatusBadge from "../../components/common/table/StatusBadge.tsx";

const RestaurantesPage = () => {
    const {
        restaurantes,
        loading,
        handleSubmit,
        handleToggleEstado,
        handleDelete } = useRestaurantes()

    const [
        activeTab,
        setActiveTab,
    ] = useState(
        'restaurantes',
    )

    const [
        isModalOpen,
        setIsModalOpen,
    ] = useState(false)

    const [
        selectedRestaurante,
        setSelectedRestaurante,
    ] = useState<Restaurante | null>(
        null,
    )

    const handleCreate = () => {
        setSelectedRestaurante(null)

        setIsModalOpen(true)
    }

    const handleEdit = (
        restaurante: Restaurante,
    ) => {
        setSelectedRestaurante(
            restaurante,
        )

        setIsModalOpen(true)
    }

    return (
        <div>
            <PageHeader
                title="Restaurantes"
                subtitle="Administración de restaurantes y menús"
                action={
                    <Button
                        onClick={
                            handleCreate
                        }
                    >
                        + Nuevo Restaurante
                    </Button>
                }
            />

            <Tabs
                activeTab={activeTab}
                onChange={setActiveTab}
                tabs={[
                    {
                        key: 'restaurantes',

                        label:
                            'Restaurantes',
                    },

                    {
                        key: 'categorias',

                        label:
                            'Categorías',
                    },

                    {
                        key: 'productos',

                        label:
                            'Productos',
                    },
                ]}
            />

            {activeTab ===
                'restaurantes' && (
                    <DataTable
                        loading={loading}
                        data={restaurantes}
                        columns={[
                            { key: 'nombre',     title: 'Nombre' },
                            { key: 'tipo_comida', title: 'Tipo' },
                            { key: 'direccion',  title: 'Dirección' },
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
                )}

            <Modal
                isOpen={isModalOpen}
                title={
                    selectedRestaurante
                        ? 'Editar Restaurante'
                        : 'Nuevo Restaurante'
                }
                onClose={() =>
                    setIsModalOpen(false)
                }
            >
                <RestauranteForm
                    key = {selectedRestaurante?.id ?? 'new'}
                    restaurante={
                        selectedRestaurante
                    }
                    onClose={() =>
                        setIsModalOpen(false)
                    }
                    onSubmit={async (body) => handleSubmit(body, selectedRestaurante)}
                />
            </Modal>
        </div>
    )
}

export default RestaurantesPage