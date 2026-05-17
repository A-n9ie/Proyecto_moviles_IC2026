import TableEmpty from './TableEmpty'

import TableLoader from './TableLoader'

import './dataTable.css'

interface Props<T> {
    columns: {
        key: string

        title: string

        render?: (item: T) => React.ReactNode
    }[]

    data: T[]

    loading?: boolean
}

const DataTable = <T,>({
                           columns,
                           data,
                           loading = false,
                       }: Props<T>) => {
    if (loading) {
        return <TableLoader />
    }

    if (!data.length) {
        return <TableEmpty />
    }

    return (
        <div className="table-container">
            <table className="custom-table">
                <thead>
                <tr>
                    {columns.map((column) => (
                        <th key={column.key}>
                            {column.title}
                        </th>
                    ))}
                </tr>
                </thead>

                <tbody>
                {data.map((item, index) => (
                    <tr key={index}>
                        {columns.map((column) => (
                            <td key={column.key}>
                                {column.render
                                    ? column.render(item)
                                    : String(
                                        item[
                                            column.key as keyof T
                                            ],
                                    )}
                            </td>
                        ))}
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    )
}

export default DataTable