package net.kankantari.ojima.scores

enum class EnumLiteralType {
    Forward, Backward, Rest, Extend; // 順はこのまま

    fun toTokenType(): EnumTokenType {
        return EnumTokenType.entries[ordinal];
    }
}