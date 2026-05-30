package com.example.cletaeats_mobile.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RestauranteEntity::class],
    version  = 1,
    exportSchema = false
)
abstract class CletaEatsDatabase : RoomDatabase() {
    abstract fun restauranteDao(): RestauranteDao

    companion object {
        @Volatile private var INSTANCE: CletaEatsDatabase? = null

        fun getInstance(context: Context): CletaEatsDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CletaEatsDatabase::class.java,
                    "cletaeats.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}