package net.kankantari.ojima

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

class Config {
    var port: Int = DEFAULT_PORT
        get() {
            return field
        }
        private set(value) {
            field = value
        }

    var processCachePath: String = DEFAULT_PROCESS_CACHE_PATH
        get() {
            return field
        }
        private set(value) {
            field = value
        }

    var deleteOriginalVideo: Boolean = DEFAULT_DELETE_ORIGINAL_VIDEO
        get() {
            return field
        }
        private  set(value) {
            field = value
        }

    var deleteProcessedVideo: Boolean = DEFAULT_DELETE_PROCESSED_VIDEO
        get() {
            return field
        }
        private set(value) {
            field = value
        }

    var adminPassword: String = UUID.randomUUID().toString().replace("-", "")
        get() {
            return field
        }
        private set(value) {
            field = value
        }

    fun save(path: String = configFile) {
        val jsonStr = GsonBuilder().setPrettyPrinting().create().toJson(this)
        val file = File(path)

        if (file.exists()) {
            file.delete()
        }

        file.createNewFile()
        file.writeText(jsonStr, Charsets.UTF_8)
    }

    companion object {
        const val configFile = "./config.json"

        const val DEFAULT_PORT = 8080
        const val DEFAULT_PROCESS_CACHE_PATH = "./process_cache"
        const val DEFAULT_DELETE_ORIGINAL_VIDEO = true
        const val DEFAULT_DELETE_PROCESSED_VIDEO = true

        lateinit var config: Config

        fun load(path: String = configFile) {
            val file = File(path)
            if (!file.exists()) {
                val conf = Config()
                conf.save()
            }
            
            val str = file.readText(Charsets.UTF_8)

            val type = object: TypeToken<Config>() {}.type

            config =  Gson().fromJson(str, type)
        }
    }
}