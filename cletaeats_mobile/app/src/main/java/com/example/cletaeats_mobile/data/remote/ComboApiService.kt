package com.example.cletaeats_mobile.data.remote

import com.example.cletaeats_mobile.domain.model.Combo
import com.example.cletaeats_mobile.domain.model.Restaurante
import com.example.cletaeats_mobile.domain.model.RestauranteConCombos
import org.json.JSONObject

class ComboApiService {

    suspend fun obtenerCombos(
        restauranteId: Int,
        token:         String
    ): Pair<Int, RestauranteConCombos?> {
        val (status, json) = ApiClient.request(
            "GET", "/combos?restaurante=$restauranteId", token = token
        )
        return Pair(status, if (status == 200) parseRespuesta(json) else null)
    }

    private fun parseRespuesta(json: String): RestauranteConCombos? = try {
        val obj  = JSONObject(json)
        val rObj = obj.getJSONObject("restaurante")

        val restaurante = Restaurante(
            id        = rObj.getInt("id"),
            nombre    = rObj.getString("nombre"),
            tipComida = rObj.getString("tipo_comida"),
            direccion = rObj.getString("direccion"),
            imagenUrl = rObj.optString("imagen_url", "")
        )

        val arr    = obj.getJSONArray("combos")
        val combos = (0 until arr.length()).map { i ->
            val c = arr.getJSONObject(i)
            Combo(
                id            = c.getInt("id"),
                restauranteId = restaurante.id,
                numeroCombo   = c.getInt("numero_combo"),
                nombre        = c.getString("nombre"),
                descripcion   = c.optString("descripcion", ""),
                precio        = c.getDouble("precio"),
                imagenUrl     = c.optString("imagen_url", "")
            )
        }
        RestauranteConCombos(restaurante, combos)
    } catch (_: Exception) { null }
}