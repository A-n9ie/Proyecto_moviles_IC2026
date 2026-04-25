package com.example.cletaeats_mobile.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    // ================================================================
    // EMULADOR (activo): 10.0.2.2 = localhost del PC desde el emulador
    // DISPOSITIVO FÍSICO: cambiar a la IP local del PC en la red WiFi
    //   const val BASE_URL = "http://192.168.1.105:8000"
    // ================================================================
    const val BASE_URL = "http://10.0.2.2:8000"

    private const val TIMEOUT_CONNECT = 5_000
    private const val TIMEOUT_READ    = 10_000

    /**
     * Coroutine-safe: ya corre en Dispatchers.IO gracias al
     * withContext en los repositorios. No bloquea el Main Thread.
     */
    suspend fun request(
        method: String,
        path:   String,
        body:   String? = null,
        token:  String? = null
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        try {
            conn.requestMethod  = method
            conn.connectTimeout = TIMEOUT_CONNECT
            conn.readTimeout    = TIMEOUT_READ
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")

            if (!token.isNullOrBlank()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }

            if (body != null) {
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { w ->
                    w.write(body); w.flush()
                }
            }

            val code   = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text   = stream?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""
            Pair(code, text)

        } catch (e: Exception) {
            Pair(-1, """{"error":"Sin conexión: ${e.message}"}""")
        } finally {
            conn.disconnect()
        }
    }
}