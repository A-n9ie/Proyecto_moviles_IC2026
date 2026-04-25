package com.example.cletaeats_mobile.data.remote

import org.json.JSONObject

class AuthApiService {

    data class AuthResponse(
        val token:     String,
        val idUsuario: Int,
        val email:     String,
        val rol:       String,
        val nombre:    String,
        val idPerfil:  Int
    )

    suspend fun login(email: String, password: String): Pair<Int, AuthResponse?> {
        val body = JSONObject().put("email", email).put("password", password).toString()
        val (status, json) = ApiClient.request("POST", "/auth/login", body)
        return Pair(status, if (status == 200) parseAuth(json) else null)
    }

    suspend fun registroCliente(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, direccion: String,
        telefono: String, tarjeta: String
    ): Pair<Int, AuthResponse?> {
        val body = JSONObject()
            .put("email", email).put("password", password)
            .put("confirmar_password", confirmarPassword)
            .put("cedula", cedula).put("nombre", nombre)
            .put("direccion", direccion).put("telefono", telefono)
            .put("tarjeta", tarjeta).toString()
        val (status, json) = ApiClient.request("POST", "/auth/registro/cliente", body)
        return Pair(status, if (status == 201) parseAuth(json) else null)
    }

    suspend fun registroRepartidor(
        email: String, password: String, confirmarPassword: String,
        cedula: String, nombre: String, correoContacto: String,
        direccion: String, telefono: String, tarjeta: String
    ): Pair<Int, AuthResponse?> {
        val body = JSONObject()
            .put("email", email).put("password", password)
            .put("confirmar_password", confirmarPassword)
            .put("cedula", cedula).put("nombre", nombre)
            .put("correo_contacto", correoContacto)
            .put("direccion", direccion).put("telefono", telefono)
            .put("tarjeta", tarjeta).toString()
        val (status, json) = ApiClient.request("POST", "/auth/registro/repartidor", body)
        return Pair(status, if (status == 201) parseAuth(json) else null)
    }

    suspend fun logout(token: String): Int =
        ApiClient.request("POST", "/auth/logout", token = token).first

    fun parseError(json: String): String = try {
        JSONObject(json).getString("error")
    } catch (_: Exception) { "Error desconocido" }

    private fun parseAuth(json: String): AuthResponse? = try {
        val o = JSONObject(json)
        AuthResponse(
            token     = o.getString("token"),
            idUsuario = o.getInt("id_usuario"),
            email     = o.getString("email"),
            rol       = o.getString("rol"),
            nombre    = o.getString("nombre"),
            idPerfil  = o.getInt("id_perfil")
        )
    } catch (_: Exception) { null }
}