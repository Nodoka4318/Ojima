package net.kankantari.ojima.routes.models

import com.google.gson.Gson

class OjimaStatus(val id: String, val status: String, val message: String, val success: Boolean) {
    fun json(): String {
        return Gson().toJson(this)
    }

    companion object {
        const val HEADER_NAME = "Ojima-Status"
    }
}