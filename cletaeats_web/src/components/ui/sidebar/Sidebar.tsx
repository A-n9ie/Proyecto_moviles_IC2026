import { NavLink, useNavigate } from 'react-router-dom'

import { sidebarItems } from '../../../constants/sidebarItems'
import { useAuth } from '../../../hooks/useAuth'

import './sidebar.css'

const Sidebar = () => {
    const navigate = useNavigate()

    const { logout, user } = useAuth()

    const handleLogout = async () => {
        logout()

        navigate('/login')
    }

    return (
        <aside className="sidebar">
            <div className="sidebar-brand">
                <div className="sidebar-logo">
                    🛵 CletaEats
                </div>

                <div className="sidebar-subtitle">
                    Panel Admin
                </div>
            </div>

            <nav className="sidebar-nav">
                {sidebarItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({ isActive }) =>
                            `sidebar-link ${isActive ? 'active' : ''}`
                        }
                    >
                        <span>{item.icon}</span>
                        <span>{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-footer">
                <div className="sidebar-user">
                    <div className="sidebar-avatar">
                        {user?.nombre?.charAt(0).toUpperCase()}
                    </div>

                    <div>
                        <div className="sidebar-user-name">
                            {user?.nombre}
                        </div>

                        <div className="sidebar-user-role">
                            Administrador
                        </div>
                    </div>
                </div>

                <button
                    className="logout-button"
                    onClick={handleLogout}
                >
                    ⬅ Cerrar sesión
                </button>
            </div>
        </aside>
    )
}

export default Sidebar