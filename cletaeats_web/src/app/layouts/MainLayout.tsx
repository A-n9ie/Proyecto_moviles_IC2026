import {Outlet} from 'react-router-dom'

import Sidebar from '../../components/ui/sidebar/Sidebar'

const MainLayout = () => {
    return (
        <div className="min-h-screen bg-[var(--gris-oscuro)] text-[var(--blanco)] flex">

            <Sidebar />

            <main className="flex-1 p-[100px] overflow-x-hidden min-h-screen">
                <Outlet />
            </main>

        </div>
    )
}

export default MainLayout