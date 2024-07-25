package net.kankantari.ojima.ojimizing.easings

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import net.kankantari.ojima.errors.OjimaError
import net.kankantari.ojima.getResourceAsText
import java.io.File

class Easing(@Expose val name: String, @Expose val expression: String) {
    private lateinit var expr: Expression

    init {
        expr = Expression(expression)
    }

    // gsonからだとinitが実行されない
    private fun initExpr() {
        expr = Expression(expression)
    }

    fun ease(frameList: List<Int>, reverse: Boolean = false): List<Int> {
        val eased =  frameList.indices.map { // 0から始まるリストに変換
            it.toDouble() / (frameList.size - 1)
        }.map {
            (expr.evaluate(it) * (frameList.size - 1)).toInt()
        }

        if (reverse) {
            return eased.map {
                frameList.last() - it // 反転し、元の範囲に戻す
            }
        } else {
            return eased.map {
                it + frameList.first() // 元の範囲に戻す
            }
        }
    }

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

            defaultEasings =  GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(str, type)

            defaultEasings.forEach {
                it.initExpr() // やらないとぬるぽ
            }
        }

        fun findEasing(name: String): Easing {
            for (easing in defaultEasings) {
                if (name == easing.name) {
                    return easing
                }
            }

            throw OjimaError(
                "Easing, '$name' not found.",
                "'$name'という名前のイージングが見つかりませんでした。"
            )
        }

        fun generateDefaultEasingsJson(): String {
            return GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .excludeFieldsWithoutExposeAnnotation()
                .create().toJson(defaultEasings)
        }
    }
}