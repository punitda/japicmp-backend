package punitd.dev.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import punitd.dev.models.orderStorage

fun Route.listOrdersRoute() {
    get("/order") {
        if (orderStorage.isNotEmpty()) {
            call.respond(orderStorage)
        }
    }
}

fun Route.getOrderRoute() {
    get("/order/{id?}") {
        val id = call.parameters["id"] ?: return@get call.respondText(
            text = "Bad Request",
            status = HttpStatusCode.BadRequest
        )

        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            text = "Not Found",
            status = HttpStatusCode.NotFound
        )

        call.respond(order)
    }
}


fun Route.totalizeOrderRoute() {
    get("/order/{id?}/total") {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        val total = order.contents.sumOf { it.price.toDouble() * it.amount }
        call.respond(total)
    }
}

