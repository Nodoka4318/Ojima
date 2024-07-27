package net.kankantari.ojima.ojimizing.impl

import net.kankantari.ojima.Config
import net.kankantari.ojima.errors.OjimaError
import net.kankantari.ojima.ojimizing.Ojimizer
import net.kankantari.ojima.ojimizing.easings.Easing
import net.kankantari.ojima.scores.EnumTokenType

class EasingOjimizer() : Ojimizer("イージング", "イージングを適用します。") {
    private lateinit var easing: Easing

    override fun setOptions(options: Map<String, Any>) {
        super.setOptions(options)

        if (options.containsKey("easing")) {
            val easingName = options.get("easing") as String

            if (easingName == "custom") {
                if (!Config.config.allowCustomEasingExpression) {
                    throw OjimaError(
                        "Custom easing is selected though custom easing is not allowed in the config.",
                        "カスタムイージングは許可されていません。"
                    )
                }

                if (options.containsKey("easingExpression")) {
                    val customExpr = options.get("easingExpression") as String
                    easing = Easing("custom", customExpr)

                    return
                } else {
                    throw OjimaError(
                        "Custom easing option is set but custom expression is not provided.",
                        "イージングの式が設定されていません。"
                    )
                }
            }

            easing = Easing.findEasing(easingName)
        } else {
            throw OjimaError(
                "Ojimizer is set to Easing, but option Easing is not set.",
                "イージングの種類が選択されていません。"
            )
        }
    }

    override fun ojimizeIndex(): List<Int> {
        val easedForward = easing.ease(frameIndexSet)
        val easedBackward = easing.ease(frameIndexSet, reverse = true)

        val ojimized = mutableListOf<Int>();

        val frameSizes = mutableListOf<Int>();
        var beatByFar = 0f;
        var lastTokenType = EnumTokenType.Backward;

        for (token in this.score.tokens) {
            val frameSize = this.fps * token.length / this.bpm * 60;

            beatByFar += token.length;

            val sum = frameSizes.sum() + frameSize;
            val accurateFrameSizeByFar = this.fps * beatByFar / this.bpm * 60;
            val adjustment = accurateFrameSizeByFar - sum; // 累積ズレを修正

            frameSizes.add(Math.round(frameSize + adjustment));

            if (token.type == EnumTokenType.Forward) {
                ojimized.addAll(DefaultOjimizer.resizeList(easedForward, frameSizes.last()));
            } else if (token.type == EnumTokenType.Backward) {
                ojimized.addAll(DefaultOjimizer.resizeList(easedBackward, frameSizes.last()));
            } else if (token.type == EnumTokenType.Rest) {
                val lastIndex = when (lastTokenType) {
                    EnumTokenType.Forward -> easedBackward.first()
                    EnumTokenType.Backward -> easedForward.first()
                    EnumTokenType.Rest -> ojimized.last()
                }

                ojimized.addAll(List(frameSizes.last()) { lastIndex } )
            }

            lastTokenType = token.type;
        }

        return ojimized
    }
}