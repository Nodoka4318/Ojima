package net.kankantari.ojima.ojimizing

import com.google.gson.Gson
import net.kankantari.ojima.errors.OjimaError
import net.kankantari.ojima.ojimizing.impl.DefaultOjimizer
import net.kankantari.ojima.ojimizing.impl.KakukakuOjimizer

class Ojimanager {
    class JsonOjimizerModel(val name: String, val description: String);

    companion object {
        private val ojimizers = listOf(
            DefaultOjimizer(),
            KakukakuOjimizer()
        )

        fun getOjimizer(name: String): Ojimizer {
            for(ojimizer in ojimizers) {
                if (name == ojimizer.name) {
                    return ojimizer;
                }
            }

            throw OjimaError(
                "Ojimizer with name, ${name} was not found",
                "${name}という名前のモードが見つかりませんでした。"
            )
        }

        fun generateOjimizerModelsJson(): String {
            val ojimizerModels = ojimizers.map {
                JsonOjimizerModel(it.name, it.description)
            }

            return Gson().toJson(ojimizerModels)
        }
    }
}