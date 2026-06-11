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
        handleDelete,
        handleAmonestacion
    } = useRepartidores()

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
                        key: 'disponible',
                        title: 'Disponible',
                        render: (r: Repartidor) => (
                            <StatusBadge status={r.disponible === 1 ? "Activo" : "Ocupado"} />
                        ),
                    },
                    {
                        key: 'estado',
                        title: 'Cuenta',
                        render: (r: Repartidor) => (
                            <StatusBadge status={r.estado === 1 ? "Activa" : "Bloqueado"} />
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
                                    variant={r.estado === 1 ? 'danger' : 'success'}
                                    onClick={() => handleToggleEstado(r)}
                                >
                                    {r.estado === 1 ? 'Bloquear' : 'Desbloquear'}
                                </Button>

                                <Button
                                    variant="danger"
                                    onClick={() =>
                                        handleDelete(r)
                                    }
                                >
                                    Eliminar
                                </Button>

                                <Button
                                    variant="warning"
                                    onClick={() => handleAmonestacion(r)}
                                >
                                    ⚠ Amonestar ({r.amonestaciones}/4)
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