package com.example.cletaeats_mobile.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RestauranteEntity::class,
        ComboEntity::class,
        PedidoEntity::class,
        UsuarioLocalEntity::class,
        PerfilLocalEntity::class,
        TarjetaLocalEntity::class,
        CategoriaLocalEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CletaEatsDatabase : RoomDatabase() {
    abstract fun restauranteDao(): RestauranteDao
    abstract fun comboDao(): ComboDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun usuarioLocalDao(): UsuarioLocalDao
    abstract fun perfilLocalDao(): PerfilLocalDao
    abstract fun tarjetaLocalDao(): TarjetaLocalDao
    abstract fun categoriaLocalDao(): CategoriaLocalDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS usuario_local (
                        idUsuario INTEGER PRIMARY KEY NOT NULL,
                        email     TEXT    NOT NULL,
                        nombre    TEXT    NOT NULL,
                        rol       TEXT    NOT NULL,
                        idPerfil  INTEGER NOT NULL,
                        token     TEXT    NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS perfil_local (
                        idPerfil INTEGER PRIMARY KEY NOT NULL,
                        rol TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        telefono TEXT NOT NULL,
                        direccion TEXT NOT NULL,
                        cedula TEXT NOT NULL,
                        imagenUrl TEXT NOT NULL DEFAULT '',
                        correo TEXT NOT NULL DEFAULT '',
                        tarjeta TEXT NOT NULL DEFAULT '',
                        rating REAL NOT NULL DEFAULT 0.0,
                        amonestaciones INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tarjetas_local (
                        id INTEGER PRIMARY KEY NOT NULL,
                        clienteId INTEGER NOT NULL,
                        numero TEXT NOT NULL,
                        alias TEXT NOT NULL DEFAULT '',
                        fechaVencimiento TEXT NOT NULL DEFAULT '',
                        cvv TEXT NOT NULL DEFAULT '',
                        esPrincipal INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS categorias_local (
                        id INTEGER PRIMARY KEY NOT NULL,
                        nombre TEXT NOT NULL
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
