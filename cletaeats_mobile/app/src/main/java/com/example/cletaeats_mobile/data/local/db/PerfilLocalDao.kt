package com.example.cletaeats_mobile.data.local.db

import androidx.room.*

@Dao
interface PerfilLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(perfil: PerfilLocalEntity)

    @Query("SELECT * FROM perfil_local WHERE idPerfil = :id LIMIT 1")
    suspend fun obtener(id: Int): PerfilLocalEntity?

    @Query("DELETE FROM perfil_local")
    suspend fun limpiar()
}