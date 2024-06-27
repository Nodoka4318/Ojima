package net.kankantari.score

class Token {
    var type: EnumTokenType;
    var length: Float;

    constructor(type: EnumTokenType, length: Float) {
        this.type = type;
        this.length = length;
    }
}