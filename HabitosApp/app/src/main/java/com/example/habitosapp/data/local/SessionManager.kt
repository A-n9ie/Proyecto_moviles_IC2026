package com.example.habitosapp.data.local

import android.content.Context

/**
 * Gestiona la sesión del usuario de forma local.
 * Almacena: token Bearer, ID de usuario y nombre de usuario.
 *
 * FUTURO: Reemplazable por EncryptedSharedPreferences o DataStore
 *         sin cambiar ninguna clase que dependa de esta.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Guarda todos los datos de sesión tras un login o registro exitoso. */
    fun saveSession(token: String, idUsuario: Int, nombreUsuario: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_ID_USUARIO, idUsuario)
            .putString(KEY_NOMBRE_USUARIO, nombreUsuario)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    /** Limpia todos los datos de sesión al hacer logout. */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean      = prefs.getBoolean(KEY_LOGGED_IN, false)
    fun getToken(): String         = prefs.getString(KEY_TOKEN, "") ?: ""
    fun getIdUsuario(): Int        = prefs.getInt(KEY_ID_USUARIO, -1)
    fun getNombreUsuario(): String = prefs.getString(KEY_NOMBRE_USUARIO, "") ?: ""

    companion object {
        private const val PREF_NAME        = "habitos_session"
        private const val KEY_TOKEN        = "token"
        private const val KEY_ID_USUARIO   = "id_usuario"
        private const val KEY_NOMBRE_USUARIO = "nombre_usuario"
        private const val KEY_LOGGED_IN    = "logged_in"
    }
}