package net.kankantari.ojima.scores

import net.kankantari.ojima.errors.OjimaError

class Score {
    val source: String

    constructor(source: String) {
        this.source = source;
    }

    companion object {
        fun parse(source: String): MutableList<Token> {
            var index = 0;
            var literals = source.map { Literal.findLiteral(it) }; // TODO: コメントアウト、改行無視を実装

            var tokens = mutableListOf<Token>();

            while(index <= source.length) {
                var lit = literals[index];

                if (lit.type == EnumLiteralType.Extend) { // 伸ばしだったとき、連続する伸ばしリテラルを全て読む
                    if (index <= 0) {
                        throw OjimaError(
                            "Extend marker can't be the first literal.",
                            "'-'を頭に記述することはできません。"
                        );
                    }

                    var markerCount = 1;

                    while (literals[++index].type == EnumLiteralType.Extend) {
                        markerCount++;
                    }

                    tokens.last().length *= markerCount + 1; // (markerCount + 1)倍する

                    continue; // indexはインクリメント済みだから、次のリテラルへ
                }

                tokens.add(Token(lit.type.toTokenType(), lit.length));

                index++;
            }

            return tokens;
        }
    }
}