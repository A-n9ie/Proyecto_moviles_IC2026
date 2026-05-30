package com.example.cletaeats_mobile.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RestauranteDao {
    @Query("SELECT * FROM restaurantes")
    suspend fun obtenerTodos(): List<RestauranteEntity>

    @Query("SELECT * FROM restaurantes WHERE id = :id")
    suspend fun obtenerPorId(id: Int): RestauranteEntity?

    @Query("SELECT * FROM restaurantes WHERE nombre LIKE '%' || :query || '%'")
    suspend fun buscar(query: String): List<RestauranteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(restaurante: RestauranteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(restaurantes: List<RestauranteEntity>)

    @Update
    suspend fun actualizar(restaurante: RestauranteEntity)

    @Delete
    suspend fun eliminar(restaurante: RestauranteEntity)

    @Query("DELETE FROM restaurantes WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}