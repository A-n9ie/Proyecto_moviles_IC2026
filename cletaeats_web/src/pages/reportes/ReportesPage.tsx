import { useEffect, useState } from 'react'
import PageHeader from '../../components/ui/pageHeader/PageHeader'
import Spinner from '../../components/common/loaders/Spinner'
import { axiosClient } from '../../services/api/axiosClient'
import './reportesPage.css'

const ReportesPage = () => {
    const [data, setData] = useState<any>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        axiosClient.get('/admin/reportes')
            .then(r => setData(r.data))
            .catch(() => setError('Error al cargar reportes'))
            .finally(() => setLoading(false))
    }, [])

    if (loading) return <div className="flex items-center justify-center py-20"><Spinner /></div>
    if (error) return <div className="text-[var(--error)]">{error}</div>

    return (
        <div>
            <PageHeader title="Reportes" subtitle="Estadísticas generales del sistema" />

            <div className="reportes-grid">

                <div className="reporte-card">
                    <h3>🏆 Restaurante con más pedidos</h3>
                    <p className="reporte-valor"><strong>{data.restaurante_mas_pedidos.nombre}</strong></p>
                    <p className="reporte-sub">{data.restaurante_mas_pedidos.total} pedidos</p>
                </div>

                <div className="reporte-card">
                    <h3>📉 Restaurante con menos pedidos</h3>
                    <p className="reporte-valor"><strong>{data.restaurante_menos_pedidos.nombre}</strong></p>
                    <p className="reporte-sub">{data.restaurante_menos_pedidos.total} pedidos</p>
                </div>

                <div className="reporte-card">
                    <h3>👤 Cliente top</h3>
                    <p className="reporte-valor"><strong>{data.cliente_top.nombre}</strong></p>
                    <p className="reporte-sub">{data.cliente_top.total} pedidos</p>
                </div>

                <div className="reporte-card">
                    <h3>⏰ Hora pico</h3>
                    <p className="reporte-valor"><strong>{data.hora_pico.hora}:00 hrs</strong></p>
                    <p className="reporte-sub">{data.hora_pico.total} pedidos en esa hora</p>
                </div>

                <div className="reporte-card">
                    <h3>💵 Monto total (todos los restaurantes)</h3>
                    <p className="reporte-valor"><strong>₡{data.monto_total_global?.monto_total?.toLocaleString()}</strong></p>
                    <p className="reporte-sub">Ventas totales del sistema</p>
                </div>
                
                <div className="reporte-card full">
                    <h3>💰 Monto total por restaurante</h3>
                    <table className="reportes-table">
                        <thead>
                            <tr>
                                <th>Restaurante</th>
                                <th>Monto</th>
                            </tr>
                        </thead>
                        <tbody>
                            {data.monto_por_restaurante.map((r: any) => (
                                <tr key={r.nombre}>
                                    <td>{r.nombre}</td>
                                    <td>₡{r.monto_total?.toLocaleString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="reporte-card full">
                    <h3>📋 Pedidos por cliente</h3>
                    {data.pedidos_por_cliente?.length === 0 && (
                        <p className="reporte-sub">No hay pedidos registrados.</p>
                    )}
                    {data.pedidos_por_cliente?.map((c: any) => (
                        <div key={c.cliente_id} className="cliente-bloque">
                            <p className="cliente-titulo">
                                <strong>{c.cliente_nombre}</strong>
                                <span className="cliente-cedula"> (cédula: {c.cliente_cedula})</span>
                                <span className="cliente-conteo"> — {c.total_pedidos} pedido(s)</span>
                            </p>
                            <table className="reportes-table">
                                <thead>
                                    <tr>
                                        <th>Pedido #</th>
                                        <th>Fecha</th>
                                        <th>Restaurante</th>
                                        <th>Estado</th>
                                        <th>Monto</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {c.pedidos.map((p: any) => (
                                        <tr key={p.pedido_id}>
                                            <td>{p.pedido_id}</td>
                                            <td>{p.fecha}</td>
                                            <td>{p.restaurante}</td>
                                            <td>{p.estado_texto}</td>
                                            <td>₡{p.monto?.toLocaleString()}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ))}
                </div>

            </div>
        </div>
    )
}

export default ReportesPage