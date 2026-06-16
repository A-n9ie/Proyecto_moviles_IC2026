package com.example.cletaeats_mobile.data.local.db

import androidx.room.*

@Dao
interface UsuarioLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(usuario: UsuarioLocalEntity)

    @Query("SELECT * FROM usuario_local WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioLocalEntity?

    @Query("DELETE FROM usuario_local")
    suspend fun limpiarTodos()
}