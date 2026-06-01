package com.example.cletaeats_mobile.data.local

import android.content.Context

class SessionManager(context: Context) {

    // SharedPrefs de sesión activa (se borra en logout)
    private val prefs = context.getSharedPreferences("cletaeats_session", Context.MODE_PRIVATE)

    // SharedPrefs de credenciales offline (NUNCA se borra en logout)
    private val offlinePrefs = context.getSharedPreferences("cletaeats_offline", Context.MODE_PRIVATE)

    fun saveSession(token: String, idUsuario: Int, email: String, rol: String, nombre: String, idPerfil: Int) {
        prefs.edit()
            .putString("token",      token)
            .putInt("id_usuario",    idUsuario)
            .putString("email",      email)
            .putString("rol",        rol)
            .putString("nombre",     nombre)
            .putInt("id_perfil",     idPerfil)
            .putBoolean("logged_in", true)
            .apply()

        // Guardar también en offline para uso sin internet
        offlinePrefs.edit()
            .putString("token",      token)
            .putInt("id_usuario",    idUsuario)
            .putString("email",      email)
            .putString("rol",        rol)
            .putString("nombre",     nombre)
            .putInt("id_perfil",     idPerfil)
            .apply()
    }

    // Borra solo la sesión activa, NO las credenciales offline
    fun clearSession() = prefs.edit().clear().apply()

    fun isLoggedIn()   = prefs.getBoolean("logged_in", false)
    fun getToken()     = prefs.getString("token", "") ?: ""
    fun getRol()       = prefs.getString("rol", "") ?: ""
    fun getNombre()    = prefs.getString("nombre", "") ?: ""
    fun getEmail()     = prefs.getString("email", "") ?: ""
    fun getIdUsuario() = prefs.getInt("id_usuario", -1)
    fun getIdPerfil()  = prefs.getInt("id_perfil", -1)

    // Para el login local — lee de offlinePrefs
    fun getOfflineEmail()     = offlinePrefs.getString("email", "") ?: ""
    fun getOfflineToken()     = offlinePrefs.getString("token", "") ?: ""
    fun getOfflineRol()       = offlinePrefs.getString("rol", "") ?: ""
    fun getOfflineNombre()    = offlinePrefs.getString("nombre", "") ?: ""
    fun getOfflineIdUsuario() = offlinePrefs.getInt("id_usuario", -1)
    fun getOfflineIdPerfil()  = offlinePrefs.getInt("id_perfil", -1)

    // ── Modo de datos ────────────────────────────────────────────
    fun saveDataMode(mode: DataMode) {
        prefs.edit().putString("data_mode", mode.name).apply()
    }

    fun getDataMode(): DataMode {
        val saved = prefs.getString("data_mode", DataMode.API_REMOTA.name)
        return DataMode.valueOf(saved ?: DataMode.API_REMOTA.name)
    }

    fun clearDataMode() {
        prefs.edit().remove("data_mode").apply()
    }
}