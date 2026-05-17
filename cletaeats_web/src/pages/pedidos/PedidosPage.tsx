import TextInput from '../../components/common/inputs/TextInput'

import DataTable from '../../components/common/table/DataTable'

import StatusBadge from '../../components/common/table/StatusBadge'

import PageHeader from '../../components/ui/pageHeader/PageHeader'

import { usePedidos } from '../../hooks/usePedidos'

import type {
    Pedido,
    PedidoColumn,
} from '../../types/pedidos'

import './pedidosPage.css'
import {formatDateTime, formatDistance} from "../../utils";

const PedidosPage = () => {
    const {
        pedidos,
        loading,
        filters,
        setFilters,
    } = usePedidos()

    // Reemplazar el array `columns` por este:
    const columns: PedidoColumn[] = [
        { key: 'id',                 title: '#' },
        { key: 'cliente_nombre',     title: 'Cliente' },
        { key: 'restaurante_nombre', title: 'Restaurante' },
        { key: 'items_count',        title: 'Items' },
        {
            key: 'distancia_km', title: 'Distancia',
            render: (p: Pedido) => formatDistance(p.distancia_km),
        },
        {
            key: 'estado', title: 'Estado',
            render: (p: Pedido) => <StatusBadge status={p.estado_texto ?? String(p.estado)} />,
        },
        {
            key: 'fecha_creacion', title: 'Fecha',
            render: (p: Pedido) => formatDateTime(p.fecha_creacion),
        },
    ]

    return (
        <div>
            <PageHeader
                title="Pedidos"
                subtitle="Gestión de pedidos de la plataforma"
            />

            <div className="pedidos-filters">
                <div className="pedidos-search">
                    <TextInput
                        placeholder="Buscar pedido..."
                        value={filters.search}
                        onChange={(value) =>
                            setFilters((prev) => ({
                                ...prev,

                                search: value,
                            }))
                        }
                    />
                </div>

                <select
                    className="pedidos-select"
                    value={filters.estado}
                    onChange={(e) =>
                        setFilters((prev) => ({
                            ...prev,

                            estado: e.target.value,
                        }))
                    }
                >
                    <option value="">
                        Todos
                    </option>

                    <option value="ENTREGADO">
                        Entregado
                    </option>

                    <option value="PREPARANDO">
                        Preparando
                    </option>

                    <option value="CAMINO">
                        En camino
                    </option>

                    <option value="CANCELADO">
                        Cancelado
                    </option>
                </select>
            </div>

            <DataTable
                columns={columns}
                data={pedidos}
                loading={loading}
            />
        </div>
    )
}

export default PedidosPage