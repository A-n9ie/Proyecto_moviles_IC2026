package com.example.cletaeats_mobile.data.local.db

import androidx.room.*

@Dao
interface TarjetaLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(tarjetas: List<TarjetaLocalEntity>)

    @Query("SELECT * FROM tarjetas_local WHERE clienteId = :clienteId")
    suspend fun obtenerPorCliente(clienteId: Int): List<TarjetaLocalEntity>

    @Query("DELETE FROM tarjetas_local")
    suspend fun limpiarTodas()
}