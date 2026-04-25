package com.example.cletaeats_mobile.data.remote

import com.example.cletaeats_mobile.domain.model.Restaurante
import org.json.JSONArray
import org.json.JSONObject

class RestauranteApiService {

    suspend fun obtenerRestaurantes(token: String): Pair<Int, List<Restaurante>> {
        val (status, json) = ApiClient.request("GET", "/restaurantes", token = token)
        return Pair(status, if (status == 200) parseLista(json) else emptyList())
    }

    private fun parseLista(json: String): List<Restaurante> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { parseRestaurante(arr.getJSONObject(it)) }
    } catch (_: Exception) { emptyList() }

    private fun parseRestaurante(o: JSONObject) = Restaurante(
        id        = o.getInt("id"),
        nombre    = o.getString("nombre"),
        tipComida = o.getString("tipo_comida"),
        direccion = o.getString("direccion"),
        imagenUrl = o.optString("imagen_url", ""),
        estado    = o.getInt("estado")
    )

    fun parseError(json: String): String = try {
        JSONObject(json).getString("error")
    } catch (_: Exception) { "Error desconocido" }
}