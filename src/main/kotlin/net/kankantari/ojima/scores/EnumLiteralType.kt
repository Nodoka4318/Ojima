package net.kankantari.ojima.scores

enum class EnumLiteralType {
    Forward, Backward, Rest, Extend, LBracket, RBracket, Comma; // 順はこのまま

    fun toTokenType(): EnumTokenType {
        return EnumTokenType.entries[ordinal];
    }
}