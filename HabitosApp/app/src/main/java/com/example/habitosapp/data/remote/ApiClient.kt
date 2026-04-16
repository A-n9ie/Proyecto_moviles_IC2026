package com.example.habitosapp.data.remote

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    // ================================================================
    // URL BASE DEL SERVIDOR
    //
    // EMULADOR ANDROID (configuración actual):
    //   10.0.2.2 es el alias especial que el emulador Android usa
    //   para apuntar al localhost del PC donde corre el servidor.
    //   Dejar esta línea activa para pruebas con emulador.
    //
    // DISPOSITIVO FÍSICO (cuando sea necesario):
    //   1. Comentar la línea de 10.0.2.2
    //   2. Descomentar la línea de IP y reemplazar con la IP real del PC
    //   3. El PC y el dispositivo deben estar en la misma red WiFi
    //   4. Verificar que el firewall del PC permita el puerto 8000
    //   5. En network_security_config.xml agregar el dominio de la IP
    // ================================================================
    const val BASE_URL = "http://10.0.2.2:8000"
    // const val BASE_URL = "http://192.168.1.105:8000"  // ← DISPOSITIVO FÍSICO

    private const val TIMEOUT_CONNECT_MS = 5_000
    private const val TIMEOUT_READ_MS    = 10_000

    /**
     * Ejecuta una petición HTTP y retorna (statusCode, responseBody).
     *
     * @param method   "GET", "POST", "PUT" o "DELETE"
     * @param path     Ruta relativa. Ej: "/auth/login"
     * @param body     JSON string para POST/PUT. Null para GET/DELETE.
     * @param token    Bearer token. Null si la ruta no requiere auth.
     * @return         Pair(statusCode, responseBodyString)
     *                 statusCode = -1 indica error de red (sin respuesta del servidor).
     */
    fun request(
        method: String,
        path: String,
        body: String? = null,
        token: String? = null
    ): Pair<Int, String> {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        return try {
            conn.requestMethod    = method
            conn.connectTimeout   = TIMEOUT_CONNECT_MS
            conn.readTimeout      = TIMEOUT_READ_MS
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")

            if (!token.isNullOrBlank()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }

            if (body != null) {
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { w ->
                    w.write(body)
                    w.flush()
                }
            }

            val code   = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text   = stream?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""
            Pair(code, text)

        } catch (e: Exception) {
            // Error de red: sin conexión, timeout, servidor caído, etc.
            Pair(-1, """{"error":"Error de red: ${e.message}"}""")
        } finally {
            conn.disconnect()
        }
    }
}