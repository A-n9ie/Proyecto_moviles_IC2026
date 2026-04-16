package com.example.habitosapp.data.remote

import org.json.JSONObject

class AuthApiService {

    data class AuthResponse(
        val token: String,
        val idUsuario: Int,
        val nombreUsuario: String
    )

    /** POST /auth/login */
    fun login(nombreUsuario: String, password: String): Pair<Int, AuthResponse?> {
        val body = JSONObject()
            .put("nombre_usuario", nombreUsuario)
            .put("password", password)
            .toString()

        val (status, json) = ApiClient.request("POST", "/auth/login", body)
        return Pair(status, if (status == 200) parseAuth(json) else null)
    }

    /** POST /auth/registro */
    fun registro(
        nombreUsuario: String,
        email: String,
        password: String,
        confirmarPassword: String
    ): Pair<Int, AuthResponse?> {
        val body = JSONObject()
            .put("nombre_usuario", nombreUsuario)
            .put("email", email)
            .put("password", password)
            .put("confirmar_password", confirmarPassword)
            .toString()

        val (status, json) = ApiClient.request("POST", "/auth/registro", body)
        return Pair(status, if (status == 201) parseAuth(json) else null)
    }

    /** POST /auth/logout */
    fun logout(token: String): Int {
        return ApiClient.request("POST", "/auth/logout", token = token).first
    }

    fun parseErrorMessage(json: String): String = try {
        JSONObject(json).getString("error")
    } catch (_: Exception) { "Error desconocido" }

    private fun parseAuth(json: String): AuthResponse? = try {
        val obj = JSONObject(json)
        AuthResponse(
            token         = obj.getString("token"),
            idUsuario     = obj.getInt("id_usuario"),
            nombreUsuario = obj.getString("nombre_usuario")
        )
    } catch (_: Exception) { null }
}