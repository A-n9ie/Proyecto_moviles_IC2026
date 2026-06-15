import {
    Navigate,
    Route,
    Routes,
} from 'react-router-dom'

import MainLayout from '../../app/layouts/MainLayout'

import ProtectedRoute from './ProtectedRoute'

import LoginPage from '../../pages/auth/LoginPage'

import DashboardPage from '../../pages/dashboard/DashboardPage'
import PedidosPage from '../../pages/pedidos/PedidosPage'
import ClientesPage from '../../pages/clientes/ClientesPage'
import RepartidoresPage from '../../pages/repartidores/RepartidoresPage'
import RestaurantesPage from '../../pages/restaurantes/RestaurantesPage'
import CombosPage from '../../pages/combos/CombosPage'
import ReportesPage from '../../pages/reportes/ReportesPage'
import QuejasPage from '../../pages/quejas/QuejasPage'
import BitacoraPage from '../../pages/admin/BitacoraPage'

const AppRouter = () => {
    return (
        <Routes>
            <Route
                path="/login"
                element={<LoginPage />}
            />

            <Route
                element={
                    <ProtectedRoute>
                        <MainLayout />
                    </ProtectedRoute>
                }
            >
                <Route
                    path="/"
                    element={<DashboardPage />}
                />

                <Route
                    path="/pedidos"
                    element={<PedidosPage />}
                />

                <Route
                    path="/clientes"
                    element={<ClientesPage />}
                />

                <Route
                    path="/repartidores"
                    element={<RepartidoresPage />}
                />

                <Route
                    path="/restaurantes"
                    element={<RestaurantesPage />}
                />

                <Route
                    path="/combos"
                    element={<CombosPage />}
                />

                <Route
                    path="/reportes"
                    element={<ReportesPage />}
                />

                <Route
                    path="/bitacora"
                    element={<BitacoraPage />}
                />

                <Route
                    path="/quejas"
                    element={<QuejasPage />}
                />
            </Route>

            <Route
                path="*"
                element={<Navigate to="/" />}
            />
        </Routes>
    )
}

export default AppRouter