import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet'
import L from 'leaflet'

interface MapaPickerProps {
    latitud: number | null
    longitud: number | null
    onChange: (lat: number, lng: number) => void
}

// Soluciona el problema común de los iconos en Vite
const markerIcon = new L.Icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41]
})

function MapClickHandler({
                             onChange
                         }: {
    onChange: (lat: number, lng: number) => void
}) {
    useMapEvents({
        click(e) {
            onChange(e.latlng.lat, e.latlng.lng)
        }
    })

    return null
}

const MapaPicker = ({
                        latitud,
                        longitud,
                        onChange
                    }: MapaPickerProps) => {
    const center: [number, number] = [
        latitud ?? 9.9341,
        longitud ?? -84.0877
    ]

    return (
        <>
            <div
                style={{
                    height: 220,
                    borderRadius: 12,
                    overflow: 'hidden',
                    marginBottom: 8
                }}
            >
                <MapContainer
                    center={center}
                    zoom={13}
                    style={{
                        width: '100%',
                        height: '100%'
                    }}
                >
                    <TileLayer
                        attribution="&copy; OpenStreetMap contributors"
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    />

                    <MapClickHandler onChange={onChange} />

                    {latitud !== null && longitud !== null && (
                        <Marker
                            position={[latitud, longitud]}
                            icon={markerIcon}
                        />
                    )}
                </MapContainer>
            </div>

            <p
                style={{
                    fontSize: 12,
                    color: '#aaa',
                    marginTop: 0
                }}
            >
                {latitud !== null && longitud !== null
                    ? `📍 ${latitud.toFixed(5)}, ${longitud.toFixed(5)}`
                    : 'Hacé clic en el mapa para marcar la ubicación'}
            </p>
        </>
    )
}

export default MapaPicker