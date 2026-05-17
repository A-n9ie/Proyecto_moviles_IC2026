import Select from 'react-select'

interface Option<T> {
    label: string

    value: T
}

interface Props<T> {
    options: Option<T>[]

    selected: T[]

    onChange: (
        values: T[],
    ) => void

    getKey: (
        item: T,
    ) => string | number

    placeholder?: string
}

const MultiSelect = <T,>({
                             options,
                             selected,
                             onChange,
                             getKey,
                             placeholder,
                         }: Props<T>) => {
    const selectedOptions =
        options.filter(
            (option) =>
                selected.some(
                    (
                        selectedItem,
                    ) =>
                        getKey(
                            selectedItem,
                        ) ===
                        getKey(
                            option.value,
                        ),
                ),
        )

    return (
        <Select
            isMulti
            options={options}
            value={selectedOptions}
            placeholder={
                placeholder
            }
            onChange={(
                values,
            ) => {
                onChange(
                    values.map(
                        (value) =>
                            value.value,
                    ),
                )
            }}
            classNamePrefix="react-select"
        />
    )
}

export default MultiSelect