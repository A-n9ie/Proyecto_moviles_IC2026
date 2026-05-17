import Spinner from '../../components/common/loaders/Spinner'

import StatsCard from '../../components/common/cards/StatsCard'

import DashboardSection from '../../components/common/cards/DashboardSection'

import PageHeader from '../../components/ui/pageHeader/PageHeader'

import { useDashboard } from '../../hooks/useDashboard'

import './dashboardPage.css'
import {formatColones} from "../../utils";

const DashboardPage = () => {
    const {
        data,
        loading,
        error,
    } = useDashboard()

    if (loading) {
        return (
            <div className="flex items-center justify-center py-20">
                <Spinner />
            </div>
        )
    }

    if (error || !data) {
        return (
            <div className="text-[var(--error)]">
                {error}
            </div>
        )
    }

    return (
        <div>
            <PageHeader
                title="Dashboard"
                subtitle="Resumen general de la plataforma"
            />

            <div className="dashboard-grid">
                <StatsCard
                    title="Pedidos Totales"
                    value={data.stats.totalPedidos}
                    icon="📦"
                />

                <StatsCard
                    title="Pedidos Activos"
                    value={data.stats.pedidosActivos}
                    icon="🔥"
                />

                <StatsCard
                    title="Clientes"
                    value={data.stats.totalClientes}
                    icon="👤"
                />

                <StatsCard
                    title="Repartidores Activos"
                    value={
                        data.stats.repartidoresActivos
                    }
                    icon="🏍️"
                />
            </div>

            <div className="dashboard-content-grid">
                <DashboardSection title="Pedidos Recientes">
                    <table className="dashboard-table">
                        <thead>
                        <tr>
                            <th>Cliente</th>
                            <th>Restaurante</th>
                            <th>Total</th>
                            <th>Estado</th>
                        </tr>
                        </thead>

                        <tbody>
                        {data.pedidosRecientes.map(
                            (pedido) => (
                                <tr key={pedido.id}>
                                    <td>{pedido.cliente}</td>

                                    <td>
                                        {pedido.restaurante}
                                    </td>

                                    <td>{formatColones(pedido.total)}</td>

                                    <td>
                      <span
                          className={`estado-badge ${pedido.estado.toLowerCase()}`}
                      >
                        {pedido.estado}
                      </span>
                                    </td>
                                </tr>
                            ),
                        )}
                        </tbody>
                    </table>
                </DashboardSection>

                <DashboardSection title="Repartidores">
                    <div className="repartidor-list">
                        {data.repartidores.map(
                            (repartidor) => (
                                <div
                                    key={repartidor.id}
                                    className="repartidor-item"
                                >
                                    <div className="repartidor-name">
                                        {repartidor.nombre}
                                    </div>

                                    <span
                                        className={`estado-badge ${repartidor.estado.toLowerCase()}`}
                                    >
                    {repartidor.estado}
                  </span>
                                </div>
                            ),
                        )}
                    </div>
                </DashboardSection>
            </div>
        </div>
    )
}

export default DashboardPage