package net.kankantari.ojima.errors

class OjimaError(val msg: String, val webErrorMsg: String = msg) : Error(msg) {

}