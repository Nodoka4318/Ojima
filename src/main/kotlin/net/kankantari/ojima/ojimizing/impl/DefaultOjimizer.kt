package net.kankantari.ojima.ojimizing.impl

import net.kankantari.ojima.ojimizing.Ojimizer
import net.kankantari.ojima.scores.EnumTokenType

class DefaultOjimizer : Ojimizer {
    constructor() : super("デフォルト", "普通の大島");

    override fun ojimizeIndex(): List<Int> {
        var ojimized = mutableListOf<Int>();

        var frameSizes = mutableListOf<Int>();
        var beatByFar = 0f;

        val reversedIndexSet = this.frameIndexSet.reversed();

        for (token in this.score.tokens) {
            val frameSize = this.fps * token.length / this.bpm * 60;

            beatByFar += token.length;

            val sum = frameSizes.sum() + frameSize;
            val accurateFrameSizeByFar = this.fps * beatByFar / this.bpm * 60;
            val adjustment = accurateFrameSizeByFar - sum; // 累積ズレを修正

            frameSizes.add(Math.round(frameSize + adjustment));

            if (token.type == EnumTokenType.Forward) {
                ojimized.addAll(resizeList(this.frameIndexSet, frameSizes.last()));
            } else if (token.type == EnumTokenType.Backward) {
                ojimized.addAll(resizeList(reversedIndexSet, frameSizes.last()));
            } else if (token.type == EnumTokenType.Rest) {
                // 最後のフレームを繰り返す
                val lastIndex = ojimized.last();

                for (i in 0..frameSizes.last()) {
                    ojimized.add(lastIndex);
                }
            }
        }

        return ojimized;
    }

    companion object {
        fun resizeList(original: List<Int>, newSize: Int): List<Int> {
            // 初めと終わりはそれぞれ一回ずつにしたいから、まずはそれらを除く
            val sliced = original.slice(1..original.size - 2);
            val slicedNewSize = newSize - 2;

            val result = mutableListOf<Int>();
            val step = sliced.size.toDouble() / slicedNewSize;

            for (i in 0 until slicedNewSize) {
                val index = (i * step).toInt();
                result.add(sliced[index]);
            }

            result.add(0, original.first());
            result.add(original.last());

            return result;
        }

    }
}