package net.kankantari.ojima.ojimizing.easings

import net.kankantari.ojima.errors.OjimaError
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class Expression(private val expression: String) {
    private val engine = ScriptEngineManager().getEngineByName("nashorn")
    private val allowedCharacters = "^[a-zA-Z0-9+\\-*/()=!?><:,. ]*".toRegex()
    private val allowedFunctions = listOf("Math.sin", "Math.cos", "Math.pow", "Math.sqrt", "Math.PI")

    init {
        if (!isValid(expression)) {
            throw OjimaError("Invalid expression", "式の解析に失敗しました。")
        }
    }

    /**
     * ガバガバスクリプトインジェクション対策
     */
    private fun isValid(expression: String): Boolean {
        if (!expression.matches(allowedCharacters)) {
            return false
        }

        val tokens = expression.split(Regex("[^a-zA-Z0-9_]"))
        tokens.filter { it.isNotEmpty() && it.startsWith("Math.") }.forEach {
            if (it !in allowedFunctions) {
                return false
            }
        }

        return true
    }

    fun evaluate(x: Double): Double {
        engine.put("x", x)
        return try {
            (engine.eval(expression) as Number).toDouble()
        } catch (e: ScriptException) {
            e.printStackTrace()
            throw OjimaError("Error evaluating expression: $expression", "式の解析に失敗しました。")
        }
    }
}