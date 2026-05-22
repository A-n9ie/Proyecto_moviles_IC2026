package com.example.cletaeats_mobile.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

// ── Modelos de request/response ──────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegistroClienteRequest(
    val email: String,
    val password: String,
    @SerializedName("confirmar_password") val confirmarPassword: String,
    val cedula: String,
    val nombre: String,
    val direccion: String,
    val telefono: String
)

data class RegistroRepartidorRequest(
    val email: String,
    val password: String,
    @SerializedName("confirmar_password") val confirmarPassword: String,
    val cedula: String,
    val nombre: String,
    @SerializedName("correo_contacto") val correoContacto: String,
    val direccion: String,
    val telefono: String,
    val tarjeta: String
)

data class AuthResponse(
    val token: String,
    @SerializedName("id_usuario") val idUsuario: Int,
    val email: String,
    val rol: String,
    val nombre: String,
    @SerializedName("id_perfil") val idPerfil: Int
)

// ── Interface Retrofit ────────────────────────────────────────────
interface IAuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("auth/registro/cliente")
    suspend fun registroCliente(@Body body: RegistroClienteRequest): Response<AuthResponse>

    @POST("auth/registro/repartidor")
    suspend fun registroRepartidor(@Body body: RegistroRepartidorRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
}