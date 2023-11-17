package punitd.dev.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import punitd.dev.routes.*

fun Application.configureRouting() {
    customerRoutes()
    orderRoutes()
    reportRoutes()
}
