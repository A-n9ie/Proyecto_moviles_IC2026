package com.example.cletaeats_mobile.data.remote

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RepartidorLocationService(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun publicarUbicacion(pedidoId: Int) {
        try {
            val token = CancellationTokenSource()
            val location = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, token.token
            ).await()
            location?.let {
                firestore.collection("ubicaciones_repartidores")
                    .document(pedidoId.toString())
                    .set(mapOf(
                        "latitud"   to it.latitude,
                        "longitud"  to it.longitude,
                        "timestamp" to System.currentTimeMillis()
                    ))
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationService", "Sin permiso de ubicación")
        }
    }

    fun limpiarUbicacion(pedidoId: Int) {
        firestore.collection("ubicaciones_repartidores")
            .document(pedidoId.toString())
            .delete()
    }
}