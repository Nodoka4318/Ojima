package net.kankantari.ojima.ojimizing.easings

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.kankantari.ojima.getResourceAsText
import java.io.File

class Easing(val name: String, val expression: String) {
    companion object {
        const val EASINGS_FILE = "./easings.json"
        lateinit var defaultEasings: List<Easing>

        fun loadDefaultEasings(path: String = EASINGS_FILE) {
            val file = File(path)
            if (!file.exists()) {
                val easingsJson = getResourceAsText("/ojima/easings.json")!!
                file.writeText(easingsJson, Charsets.UTF_8)
            }

            val str = file.readText(Charsets.UTF_8)
            val type = object: TypeToken<List<Easing>>() {}.type

            defaultEasings =  Gson().fromJson(str, type)
        }
    }
}