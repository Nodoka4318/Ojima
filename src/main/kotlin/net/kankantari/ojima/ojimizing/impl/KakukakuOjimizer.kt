package net.kankantari.ojima.ojimizing.impl

import net.kankantari.ojima.ojimizing.Ojimizer
import net.kankantari.ojima.scores.EnumTokenType

class KakukakuOjimizer : Ojimizer {
    constructor() : super("カクカク", "最初のフレームと最後のフレームのみを繰り返します。");

    override fun ojimizeIndex(): List<Int> {
        var ojimized = mutableListOf<Int>();

        var frameSizes = mutableListOf<Int>();
        var beatByFar = 0f;

        for (token in this.score.tokens) {
            val frameSize = this.fps * token.length / this.bpm * 60;

            beatByFar += token.length;

            val sum = frameSizes.sum() + frameSize;
            val accurateFrameSizeByFar = this.fps * beatByFar / this.bpm * 60;
            val adjustment = accurateFrameSizeByFar - sum; // 累積ズレを修正

            frameSizes.add(Math.round(frameSize + adjustment));

            if (token.type == EnumTokenType.Forward) {
                ojimized.addAll(List(frameSizes.last()) { frameIndexSet.first() });
            } else if (token.type == EnumTokenType.Backward) {
                ojimized.addAll(List(frameSizes.last()) { frameIndexSet.last() });
            } else if (token.type == EnumTokenType.Rest) {
                // 最後のフレームを繰り返す
                val lastIndex = ojimized.last();

                ojimized.addAll(List(frameSizes.last()) { lastIndex } );
            }
        }

        return ojimized;
    };
}