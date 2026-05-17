import './statsCard.css'

interface Props {
    title: string
    value: number
    icon: string
}

const StatsCard = ({
                       title,
                       value,
                       icon,
                   }: Props) => {
    return (
        <div className="stats-card">
            <div>
                <div className="stats-card-title">
                    {title}
                </div>

                <div className="stats-card-value">
                    {value}
                </div>
            </div>

            <div className="stats-card-icon">
                {icon}
            </div>
        </div>
    )
}

export default StatsCard