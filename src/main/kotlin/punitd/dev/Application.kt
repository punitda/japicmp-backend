package punitd.dev

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import punitd.dev.di.appModule
import punitd.dev.plugins.configureRouting
import punitd.dev.plugins.configureSerialization
import punitd.dev.util.MissingFieldException

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    install(CallId) {
        generate(16)
        replyToHeader(HttpHeaders.XRequestId)
    }
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
