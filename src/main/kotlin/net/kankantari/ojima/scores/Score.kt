package net.kankantari.ojima.scores

import net.kankantari.ojima.errors.OjimaError

class Score {
    val source: String;
    val tokens: List<Token>;

    constructor(source: String) {
        this.source = source;
        this.tokens = parse(this.source);
    }

    companion object {
        fun parse(source: String): MutableList<Token> {
            var index = 0;
            val cleaned = removeComments(source);
            var literals = cleaned.map { Literal.findLiteral(it) };

            var tokens = mutableListOf<Token>();

            while(index < literals.size) {
                var lit = literals[index];

                if (lit.type == EnumLiteralType.Extend) { // 伸ばしだったとき、連続する伸ばしリテラルを全て読む
                    if (index <= 0) {
                        throw OjimaError(
                            "Extend marker can't be the first literal.",
                            "'-'を頭に記述することはできません。"
                        );
                    }

                    var markerCount = 1;

                    if (index + 1 >= literals.size) { // 最後が1つの伸ばしだったとき（d-）
                        break;
                    }

                    while (literals[++index].type == EnumLiteralType.Extend) {
                        markerCount++;

                        if (index + 1 >= literals.size) { // 最後のリテラルが連続する伸ばしだったとき（d--）
                            index++;
                            break;
                        }
                    }

                    tokens.last().length *= markerCount + 1; // (markerCount + 1)倍する

                    continue; // indexはインクリメント済みだから、次のリテラルへ
                }

                tokens.add(Token(lit.type.toTokenType(), lit.length));

                index++;
            }

            return tokens;
        }

        /**
         * コメントと空白・改行を除く
         */
        private fun removeComments(source: String): String {
            val sb = StringBuilder();
            var inComment = false;

            for (i in source.indices) {
                if (source[i] == '(') {
                    inComment = true;
                } else if (source[i] == ')') {
                    if (!inComment) {
                        throw OjimaError("Comment is not started.", "コメントがありません。");
                    }

                    inComment = false;
                } else if (!inComment && !source[i].isWhitespace()) {
                    sb.append(source[i]);
                }
            }

            if (inComment) {
                throw OjimaError("Comment is not ended.", "コメントが閉じられていません。");
            }

            return sb.toString();
        }
    }
}