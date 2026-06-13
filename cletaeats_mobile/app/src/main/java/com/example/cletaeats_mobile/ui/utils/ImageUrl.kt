package com.example.cletaeats_mobile.ui.utils

import com.example.cletaeats_mobile.data.remote.RetrofitClient

/** Devuelve null si está vacío; agrega el BASE_URL si la ruta es relativa (/uploads/...). */
fun resolveImageUrl(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return if (raw.startsWith("http://") || raw.startsWith("https://")) raw
    else RetrofitClient.BASE_URL.trimEnd('/') + raw
}