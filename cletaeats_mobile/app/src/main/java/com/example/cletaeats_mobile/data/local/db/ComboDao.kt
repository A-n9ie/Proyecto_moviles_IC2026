package com.example.cletaeats_mobile.data.local.db

import androidx.room.*

@Dao
interface ComboDao {
    @Query("SELECT * FROM combos WHERE restauranteId = :restauranteId")
    suspend fun obtenerPorRestaurante(restauranteId: Int): List<ComboEntity>

    @Query("SELECT * FROM combos")
    suspend fun obtenerTodos(): List<ComboEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(combos: List<ComboEntity>)

    @Query("DELETE FROM combos WHERE restauranteId = :restauranteId")
    suspend fun eliminarPorRestaurante(restauranteId: Int)

    @Query("DELETE FROM combos")
    suspend fun limpiarTodos()
}