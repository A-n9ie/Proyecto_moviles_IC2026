interface Props {
    title: string
    subtitle: string
    action?: React.ReactNode
}

const PageHeader = ({
                        title,
                        subtitle,
                        action,
                    }: Props) => {
    return (
        <div className="mb-6 flex items-center justify-between">
            <div>
                <h1 className="text-2xl font-bold">
                    {title}
                </h1>

                <p className="text-sm text-[var(--texto-sec)]">
                    {subtitle}
                </p>
            </div>

            {action}
        </div>
    )
}

export default PageHeader