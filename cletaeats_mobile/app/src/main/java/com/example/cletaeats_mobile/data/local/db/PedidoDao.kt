package com.example.cletaeats_mobile.data.local.db

import androidx.room.*

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos ORDER BY id DESC")
    suspend fun obtenerTodos(): List<PedidoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(pedidos: List<PedidoEntity>)

    @Query("DELETE FROM pedidos")
    suspend fun limpiarTodos()
}