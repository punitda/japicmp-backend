package punitd.dev.routes

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import punitd.dev.models.orderStorage


@Resource("/orders")
private class Order() {
    @Resource("{id?}")
    class Id(val parent: Order = Order(), val id: String) {
        @Resource("total")
        class Total(val parent: Id)
    }
}

fun Application.orderRoutes() {
    routing {
        getOrdersRoute()
        getOrderRoute()
        totalizeOrderRoute()
    }
}

fun Route.getOrdersRoute() {
    get<Order> {
        if (orderStorage.isNotEmpty()) {
            call.respond(orderStorage)
        }
    }
}

fun Route.getOrderRoute() {
    get<Order.Id> {
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
    get<Order.Id.Total> {
        val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        val total = order.contents.sumOf { it.price.toDouble() * it.amount }
        call.respond(total)
    }
}

