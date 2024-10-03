package net.kankantari.ojima.scores

import net.kankantari.ojima.errors.OjimaError

class Literal {
    val label: Char;
    val length: Float;
    val type: EnumLiteralType;

    constructor(label: Char, length: Float, type: EnumLiteralType) {
        this.label = label;
        this.length = length;
        this.type = type;
    }

    companion object {
        val literals: List<Literal> = listOf(
            //全音符系
            Literal('o', 4f, EnumLiteralType.Forward),
            Literal('c', 4f, EnumLiteralType.Backward),
            Literal('_', 4f, EnumLiteralType.Rest),
            // 四分音符系
            Literal('d', 1f, EnumLiteralType.Forward),
            Literal('b', 1f, EnumLiteralType.Backward),
            Literal('s', 1f, EnumLiteralType.Rest),
            // 八分音符系
            Literal('q', 0.5f, EnumLiteralType.Forward),
            Literal('p', 0.5f, EnumLiteralType.Backward),
            Literal('r', 0.5f, EnumLiteralType.Rest),
            // 伸ばし
            Literal('-', 0f, EnumLiteralType.Extend),
            // 連符用
            Literal('[', 0f, EnumLiteralType.LBracket),
            Literal(']', 0f, EnumLiteralType.RBracket),
            Literal(',', 0f, EnumLiteralType.Comma)
        );

        fun findLiteral(label: Char): Literal {
            var literal =  literals.find { it.label == label };

            if (literal == null) {
                throw OjimaError(
                    "'$label' is not a valid literal.",
                    "楽譜が不正です。'$label'というリテラルはありません。"
                );
            }

            return literal;
        }
    }
}