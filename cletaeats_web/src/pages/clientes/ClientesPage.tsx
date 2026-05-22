import {
    useState,
} from 'react'

import Button from '../../components/common/buttons/Button'

import DataTable from '../../components/common/table/DataTable'

import Modal from '../../components/common/modal/Modal'

import StatusBadge from '../../components/common/table/StatusBadge'

import PageHeader from '../../components/ui/pageHeader/PageHeader'

import ClienteForm from './ClienteForm'

import { useClientes } from '../../hooks/useClientes'

import type {
    Cliente,
} from '../../types/clientes'

import './clientesPage.css'

const ClientesPage = () => {
    const { clientes, loading, 
        handleSubmit, handleDelete, 
        handleToggleEstado } = useClientes()

    const [isModalOpen, setIsModalOpen] =
        useState(false)

    const [selectedCliente, setSelectedCliente] =
        useState<Cliente | null>(
            null,
        )

    const handleCreate = () => {
        setSelectedCliente(null)

        setIsModalOpen(true)
    }

    const handleEdit = (
        cliente: Cliente,
    ) => {
        setSelectedCliente(cliente)

        setIsModalOpen(true)
    }

    return (
        <div>
            <PageHeader
                title="Clientes"
                subtitle="Administración de clientes"
                action={
                    <div className="clientes-header-actions">
                        <Button onClick={handleCreate}>
                            + Nuevo Cliente
                        </Button>
                    </div>
                }
            />

            <DataTable
                loading={loading}
                data={clientes}
                columns={[
                    {
                        key: 'nombre',

                        title: 'Nombre',
                    },

                    {
                        key: 'email',

                        title: 'Correo',
                    },

                    {
                        key: 'telefono',

                        title: 'Teléfono',
                    },

                    {
                        key: 'estado',

                        title: 'Estado',

                        render: (
                            cliente: Cliente,
                        ) => (
                            <StatusBadge
                                status={
                                    cliente?.estado ? "Activo" : "Inactivo"
                                }
                            />
                        ),
                    },

                    {
                        key: 'actions',

                        title: 'Acciones',

                        render: (
                            cliente: Cliente,
                        ) => (
                            <div className="flex gap-2">
                                <Button variant="secondary" onClick={() => handleEdit(cliente)}>Editar</Button>
                                <Button
                                    variant={cliente.estado === 1 ? 'danger' : 'success'}
                                    onClick={() => handleToggleEstado(cliente)}
                                >
                                    {cliente.estado === 1 ? 'Desactivar' : 'Activar'}
                                </Button>
                                <Button variant="danger" onClick={() => handleDelete(cliente)}>Eliminar</Button>
                            </div>
                        ),
                    },
                ]}
            />

            <Modal
                isOpen={isModalOpen}
                title={
                    selectedCliente
                        ? 'Editar Cliente'
                        : 'Nuevo Cliente'
                }
                onClose={() =>
                    setIsModalOpen(false)
                }
            >
                <ClienteForm
                    key={selectedCliente?.id ?? 'new'}
                    cliente={
                        selectedCliente
                    }
                    onClose={() =>
                        setIsModalOpen(false)
                    }
                    onSubmit={(data) =>
                        handleSubmit(
                            data,
                            selectedCliente,
                        )
                    }
                />
            </Modal>

        </div>
    )
}

export default ClientesPage