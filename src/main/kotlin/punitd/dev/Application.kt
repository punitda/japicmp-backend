package punitd.dev

import io.ktor.server.application.*
import io.ktor.server.netty.*
import punitd.dev.plugins.configureRouting

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
}
