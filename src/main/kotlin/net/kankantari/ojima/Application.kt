package net.kankantari.ojima

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import net.kankantari.ojima.routes.*
import java.text.DateFormat

fun main() {
    Config.load()

    embeddedServer(Netty, port = Config.config.port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}
