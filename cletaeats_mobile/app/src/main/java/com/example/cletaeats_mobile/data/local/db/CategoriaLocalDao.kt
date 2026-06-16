package com.example.cletaeats_mobile.data.local.db

import androidx.room.*

@Dao
interface CategoriaLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(categorias: List<CategoriaLocalEntity>)

    @Query("SELECT * FROM categorias_local")
    suspend fun obtenerTodas(): List<CategoriaLocalEntity>

    @Query("DELETE FROM categorias_local")
    suspend fun limpiarTodas()
}