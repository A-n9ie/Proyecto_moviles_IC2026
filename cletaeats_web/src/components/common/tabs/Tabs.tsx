import './tabs.css'

interface Tab {
    key: string

    label: string
}

interface Props {
    tabs: Tab[]

    activeTab: string

    onChange: (
        key: string,
    ) => void
}

const Tabs = ({
                  tabs,
                  activeTab,
                  onChange,
              }: Props) => {
    return (
        <div className="tabs-container">
            {tabs.map((tab) => (
                <button
                    key={tab.key}
                    className={`tab-item ${
                        activeTab === tab.key
                            ? 'active'
                            : ''
                    }`}
                    onClick={() =>
                        onChange(tab.key)
                    }
                >
                    {tab.label}
                </button>
            ))}
        </div>
    )
}

export default Tabs