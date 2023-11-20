package punitd.dev.plugins

import io.ktor.server.application.*
import punitd.dev.routes.*

fun Application.configureRouting() {
    reportRoutes()
    preSignedUrlRoute()
}
