import './dashboardSection.css'
import type { ReactNode } from "react";

interface Props {
    title: string
    children: ReactNode
}

const DashboardSection = ({
                              title,
                              children,
                          }: Props) => {
    return (
        <section className="dashboard-section">
            <div className="dashboard-section-title">
                {title}
            </div>

            {children}
        </section>
    )
}

export default DashboardSection