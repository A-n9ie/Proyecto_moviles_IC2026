package com.example.cletaeats_mobile.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RestauranteEntity::class, ComboEntity::class, PedidoEntity::class],
    version  = 2,
    exportSchema = false
)
abstract class CletaEatsDatabase : RoomDatabase() {
    abstract fun restauranteDao(): RestauranteDao
    abstract fun comboDao(): ComboDao
    abstract fun pedidoDao(): PedidoDao
    companion object {
        @Volatile private var INSTANCE: CletaEatsDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS combos (
                        id INTEGER PRIMARY KEY NOT NULL,
                        restauranteId INTEGER NOT NULL,
                        numeroCombo INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        descripcion TEXT NOT NULL DEFAULT '',
                        precio REAL NOT NULL,
                        imagenUrl TEXT NOT NULL DEFAULT '',
                        productosJson TEXT NOT NULL DEFAULT '[]'
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS pedidos (
                        id INTEGER PRIMARY KEY NOT NULL,
                        estado INTEGER NOT NULL DEFAULT 0,
                        estadoTexto TEXT NOT NULL DEFAULT '',
                        restauranteNombre TEXT NOT NULL DEFAULT '',
                        tipoComida TEXT NOT NULL DEFAULT '',
                        clienteNombre TEXT NOT NULL DEFAULT '',
                        distanciaKm REAL NOT NULL DEFAULT 0.0,
                        fechaCreacion TEXT NOT NULL DEFAULT '',
                        fechaEntrega TEXT NOT NULL DEFAULT '',
                        itemsCount INTEGER NOT NULL DEFAULT 0,
                        restauranteLatitud REAL NOT NULL DEFAULT 0.0,
                        restauranteLongitud REAL NOT NULL DEFAULT 0.0
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): CletaEatsDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CletaEatsDatabase::class.java,
                    "cletaeats.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}