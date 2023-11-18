package punitd.dev

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import punitd.dev.plugins.configureRouting
import punitd.dev.plugins.configureSerialization
import punitd.dev.util.MissingFieldException

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(Resources)
    install(StatusPages) {
        exception<MissingFieldException> { call, cause ->
            call.respondText(text = cause.message, status = HttpStatusCode.BadRequest)
        }
        exception<BadRequestException> { call, cause ->
            call.respondText(text = cause.message ?: "error", status = HttpStatusCode.BadRequest)
        }
    }
    configureRouting()
    configureSerialization()
}
