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

            while (index < literals.size) {
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
                        tokens.last().length *= markerCount + 1; // (markerCount + 1)倍する
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

                // 連符 [dbd, d]は、dの長さでdbdを再生する
                if (lit.type == EnumLiteralType.LBracket) {
                    var innerScore = "" // dbdにあたる譜面

                    // まず、コンマまで読む
                    while (literals[index].type != EnumLiteralType.Comma) {
                        if (index + 1 >= literals.size) {
                            throw OjimaError(
                                "Incomplete tuplet expression.",
                                "不完全な連符表現です。"
                            );
                        }

                        if (literals[++index].type == EnumLiteralType.Comma) {
                            break
                        }

                        innerScore += literals[index].label
                    }

                    var innerScoreTokens = parse(innerScore)
                    var lengthScore = "" // dにあたる譜面

                    // 括弧が閉じるまで読む
                    while (literals[index].type != EnumLiteralType.RBracket) {
                        if (index + 1 >= literals.size) {
                            throw OjimaError(
                                "Incomplete tuplet expression.",
                                "不完全な連符表現です。"
                            );
                        }

                        if (literals[++index].type == EnumLiteralType.RBracket) {
                            break
                        }

                        lengthScore += literals[index].label
                    }

                    var lengthScoreTokens = parse(lengthScore)

                    // innerScoreTokensのそれぞれ長さをlengthScoreTokensの長さの合計に
                    var lengthToModify = lengthScoreTokens.sumOf { t -> t.length.toDouble() } // Double
                    var originalLength = innerScoreTokens.sumOf { t -> t.length.toDouble() }

                    for (token in innerScoreTokens) {
                        val newTokenLength = (token.length.toDouble() / originalLength) * lengthToModify
                        tokens.add(Token(token.type, newTokenLength.toFloat())) // Double -> Float
                    }

                    index++;
                    continue;
                }

                if (lit.type == EnumLiteralType.Backward || lit.type == EnumLiteralType.Forward) {
                    tokens.add(Token(lit.type.toTokenType(), lit.length));
                    index++;

                    continue;
                }

                throw OjimaError(
                    "Unexpected token: ${lit.label}",
                    "不正なトークンです。"
                )
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