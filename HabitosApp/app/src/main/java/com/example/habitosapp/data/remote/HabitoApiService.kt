package com.example.habitosapp.data.remote

import com.example.habitosapp.domain.model.Habito
import org.json.JSONArray
import org.json.JSONObject

class HabitoApiService {

    /** GET /habitos — requiere token */
    fun obtenerHabitos(token: String): Pair<Int, List<Habito>> {
        val (status, json) = ApiClient.request("GET", "/habitos", token = token)
        return Pair(status, if (status == 200) parseLista(json) else emptyList())
    }

    /** POST /habitos — requiere token */
    fun crearHabito(nombre: String, descripcion: String, token: String): Pair<Int, Habito?> {
        val body = JSONObject()
            .put("nombre", nombre)
            .put("descripcion", descripcion)
            .toString()
        val (status, json) = ApiClient.request("POST", "/habitos", body, token)
        return Pair(status, if (status == 201) parseHabito(JSONObject(json)) else null)
    }

    /** PUT /habitos/{id} — requiere token */
    fun actualizarHabito(habito: Habito, token: String): Pair<Int, Habito?> {
        val body = JSONObject()
            .put("nombre", habito.nombre)
            .put("descripcion", habito.descripcion)
            .toString()
        val (status, json) = ApiClient.request("PUT", "/habitos/${habito.id}", body, token)
        return Pair(status, if (status == 200) parseHabito(JSONObject(json)) else null)
    }

    /** DELETE /habitos/{id} — requiere token */
    fun eliminarHabito(id: Int, token: String): Int =
        ApiClient.request("DELETE", "/habitos/$id", token = token).first

    fun parseErrorMessage(json: String): String = try {
        JSONObject(json).getString("error")
    } catch (_: Exception) { "Error desconocido" }

    // ─── Helpers de parsing ─────────────────────────────────────────

    private fun parseLista(json: String): List<Habito> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { parseHabito(arr.getJSONObject(it)) }
    } catch (_: Exception) { emptyList() }

    private fun parseHabito(obj: JSONObject) = Habito(
        id               = obj.getInt("id"),
        nombre           = obj.getString("nombre"),
        descripcion      = obj.optString("descripcion", ""),
        idUsuario        = obj.optInt("id_usuario", 0),
        idTipo           = obj.optInt("id_tipo").takeIf { !obj.isNull("id_tipo") },
        idEstado         = obj.optInt("id_estado").takeIf { !obj.isNull("id_estado") },
        duracionObjetivo = obj.optInt("duracion_objetivo").takeIf { !obj.isNull("duracion_objetivo") }
    )
}