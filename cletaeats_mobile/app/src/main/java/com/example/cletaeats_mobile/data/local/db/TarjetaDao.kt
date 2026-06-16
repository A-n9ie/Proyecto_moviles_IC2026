package com.example.cletaeats_mobile.data.local.db

import androidx.room.*

@Dao
interface TarjetaDao {
    @Query("SELECT * FROM tarjetas")
    suspend fun obtenerTodas(): List<TarjetaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarjeta: TarjetaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(tarjetas: List<TarjetaEntity>)

    @Update
    suspend fun actualizar(tarjeta: TarjetaEntity)

    @Query("DELETE FROM tarjetas WHERE id = :id")
    suspend fun eliminar(id: Int)
}
