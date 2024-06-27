package net.kankantari.score

class Literal {
    val label: String;
    val length: Float;
    val type: EnumLiteralType;

    constructor(label: String, length: Float, type: EnumLiteralType) {
        this.label = label;
        this.length = length;
        this.type = type;
    }

    companion object {
        var literals: List<Literal> = listOf(
            //全音符系
            Literal("o", 4f, EnumLiteralType.Forward),
            Literal("c", 4f, EnumLiteralType.Backward),
            Literal("_", 4f, EnumLiteralType.Rest),
            // 四分音符系
            Literal("d", 1f, EnumLiteralType.Forward),
            Literal("b", 1f, EnumLiteralType.Backward),
            Literal("s", 1f, EnumLiteralType.Rest),
            // 八分音符系
            Literal("q", 0.5f, EnumLiteralType.Forward),
            Literal("p", 0.5f, EnumLiteralType.Backward),
            Literal("r", 0.5f, EnumLiteralType.Rest),
            // 伸ばし
            Literal("-", 0f, EnumLiteralType.Extend)
        );
    }
}