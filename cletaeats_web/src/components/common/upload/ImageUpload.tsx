import './imageUpload.css'

interface Props {
    value: string

    onChange: (
        value: string,
    ) => void
}

const ImageUpload = ({
                         value,
                         onChange,
                     }: Props) => {
    const handleFile =
        (
            event: React.ChangeEvent<HTMLInputElement>,
        ) => {
            const file =
                event.target.files?.[0]

            if (!file) return

            const reader =
                new FileReader()

            reader.onload = () => {
                onChange(
                    reader.result as string,
                )
            }

            reader.readAsDataURL(file)
        }

    return (
        <div className="image-upload">
            {value ? (
                <img
                    src={value}
                    alt="preview"
                    className="image-preview"
                />
            ) : (
                <div className="image-placeholder">
                    Sin imagen
                </div>
            )}

            <input
                type="file"
                accept="image/*"
                onChange={handleFile}
            />
        </div>
    )
}

export default ImageUpload