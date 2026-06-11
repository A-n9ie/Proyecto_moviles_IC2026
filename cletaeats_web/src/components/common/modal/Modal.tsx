interface Props {
    isOpen: boolean
    title: string
    children: React.ReactNode
    onClose: () => void
}

const Modal = ({
                   isOpen,
                   title,
                   children,
                   onClose,
               }: Props) => {
    if (!isOpen) return null

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/70"
            onClick={onClose}
        >
            <div
                className="w-[520px] max-h-[90vh] overflow-y-auto rounded-2xl border border-[var(--gris-borde)] bg-[var(--gris-medio)] p-7"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="mb-5 flex items-center justify-between">
                    <h2 className="text-lg font-bold">
                        {title}
                    </h2>

                    <button onClick={onClose}>
                        ✕
                    </button>
                </div>

                {children}
            </div>
        </div>
    )
}

export default Modal