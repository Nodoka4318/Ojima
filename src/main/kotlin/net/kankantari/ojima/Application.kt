package net.kankantari.ojima

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import net.kankantari.ojima.routes.*
import java.text.DateFormat
import net.kankantari.ojima.ojimizing.easings.Easing
import net.kankantari.ojima.routes.configureRouting

fun main() {
    val ascii = """
   ____    _ _                
  / __ \  (_|_)___ ___  ____ _
 / / / / / / / __ `__ \/ __ `/
/ /_/ / / / / / / / / / /_/ / 
\____/_/ /_/_/ /_/ /_/\__,_/  
    /___/                     
by Nodoka4318 (https://github.com/Nodoka4318/Ojima)
    """.trimIndent()

    println(ascii)

    Config.load()
    Easing.loadDefaultEasings()

    embeddedServer(Netty, port = Config.config.port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}

fun getResourceAsText(path: String): String? =
    object {}.javaClass.getResource(path)?.readText()
