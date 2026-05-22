import {
    useState,
} from 'react'

import Button from '../../components/common/buttons/Button'

import Modal from '../../components/common/modal/Modal'

import DataTable from '../../components/common/table/DataTable'

import StatusBadge from '../../components/common/table/StatusBadge'

import PageHeader from '../../components/ui/pageHeader/PageHeader'

import RepartidorForm from './RepartidorForm'

import { useRepartidores } from '../../hooks/useRepartidores'

import type {
    Repartidor,
} from '../../types/repartidores'

import './repartidoresPage.css'

const RepartidoresPage = () => {
    const {
        repartidores,
        loading,
        handleSubmit,
        handleToggleEstado,
        handleDelete } = useRepartidores()

    const [
        isModalOpen,
        setIsModalOpen,
    ] = useState(false)

    const [
        selectedRepartidor,
        setSelectedRepartidor,
    ] = useState<Repartidor | null>(
        null,
    )

    const handleCreate = () => {
        setSelectedRepartidor(null)

        setIsModalOpen(true)
    }

    return (
        <div>
            <PageHeader
                title="Repartidores"
                subtitle="Administración de repartidores"
                action={
                    <Button
                        onClick={
                            handleCreate
                        }
                    >
                        + Nuevo Repartidor
                    </Button>
                }
            />

            <DataTable
                loading={loading}
                data={repartidores}
                columns={[
                    {
                        key: 'nombre',

                        title: 'Nombre',
                    },

                    {
                        key: 'correo',

                        title: 'Correo',
                    },

                    {
                        key: 'telefono',

                        title: 'Teléfono',
                    },

                    {
                        key: 'amonestaciones',

                        title: 'Amonestaciones',
                    },

                    {
                        key: 'estado',

                        title: 'Estado',

                        render: (
                            repartidor: Repartidor,
                        ) => (
                            <StatusBadge
                                status={
                                    repartidor.estado ? "Activo" : "Inactivo"
                                }
                            />
                        ),
                    },

                    {
                        key: 'actions',

                        title: 'Acciones',

                        render: (r: Repartidor) => (
                            <div className="repartidores-actions">
                                <Button
                                    variant="secondary"
                                    onClick={() => {
                                        setSelectedRepartidor(r)
                                        setIsModalOpen(true)
                                    }}
                                >
                                    Editar
                                </Button>

                                <Button
                                    variant={
                                        r.estado === 1
                                            ? 'danger'
                                            : 'success'
                                    }
                                    onClick={() =>
                                        handleToggleEstado(r)
                                    }
                                >
                                    {r.estado === 1
                                        ? 'Desactivar'
                                        : 'Activar'}
                                </Button>

                                <Button
                                    variant="danger"
                                    onClick={() =>
                                        handleDelete(r)
                                    }
                                >
                                    Eliminar
                                </Button>
                            </div>
                        ),
                    },
                ]}
            />

            <Modal
                isOpen={isModalOpen}
                title={
                    selectedRepartidor
                        ? 'Editar Repartidor'
                        : 'Nuevo Repartidor'
                }
                onClose={() =>
                    setIsModalOpen(false)
                }
            >
                <RepartidorForm
                    key = {selectedRepartidor?.id ?? 'new'}
                    repartidor={
                        selectedRepartidor
                    }
                    onClose={() =>
                        setIsModalOpen(false)
                    }
                    onSubmit={async (body) => handleSubmit(body, selectedRepartidor)}
                />
            </Modal>
        </div>
    )
}

export default RepartidoresPage