package punitd.dev

import io.ktor.server.application.*
import io.ktor.server.netty.*
import punitd.dev.plugins.configureRouting
import punitd.dev.plugins.configureSerialization

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
    configureSerialization()
}
