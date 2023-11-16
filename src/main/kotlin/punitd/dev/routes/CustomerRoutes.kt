package punitd.dev.routes

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import punitd.dev.models.Customer
import punitd.dev.models.customerStorage
import io.ktor.server.resources.post
import punitd.dev.util.MissingFieldException

const val NO_CUSTOMERS_FOUND = "No customers found"
const val MISSING_CUSTOMER_ID = "Missing id in parameters"
const val NO_CUSTOMER_WITH_ID = "No customer with id"
const val CUSTOMER_STORED_SUCCESSFULLY = "Customer stored successfully"
const val CUSTOMER_REMOVED_SUCCESSFULLY = "Customer removed successfully"
const val MISSING_FIELDS_IN_REQUEST_BODY = "Missing fields in request body"
const val CUSTOMER_NOT_FOUND = "No customer found"


@Resource("/customers")
private class Customers {
    @Resource("{id?}")
    class Id(val parent: Customers = Customers(), val id: String)
}

fun Application.customerRoutes() {
    routing {
        getCustomersRoute()
        getCustomerRoute()
        addCustomerRoute()
        deleteCustomerRoute()
    }
}

fun Route.getCustomersRoute() {
    get<Customers> {
        if (customerStorage.isNotEmpty()) {
            call.respond(customerStorage)
        } else {
            call.respondText(
                text = NO_CUSTOMERS_FOUND,
                status = HttpStatusCode.OK,
            )
        }
    }
}

fun Route.getCustomerRoute() {
    get<Customers.Id> {
        val id = call.parameters["id"] ?: return@get call.respondText(
            text = MISSING_CUSTOMER_ID,
            status = HttpStatusCode.BadRequest,
        )

        val customer = customerStorage.find { it.id == id } ?: return@get call.respondText(
            text = "$NO_CUSTOMER_WITH_ID $id",
            status = HttpStatusCode.NotFound
        )
        call.respond(customer)
    }

}

fun Route.addCustomerRoute() {
    post<Customers> {
        val customer = runCatching { call.receiveNullable<Customer>() }.getOrNull()
            ?: throw MissingFieldException(MISSING_FIELDS_IN_REQUEST_BODY)
        customerStorage.add(customer)
        call.respondText(text = CUSTOMER_STORED_SUCCESSFULLY, status = HttpStatusCode.Created)
    }
}

fun Route.deleteCustomerRoute() {
    delete<Customers.Id> {
        val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        if (customerStorage.removeIf { it.id == id }) {
            call.respondText(text = CUSTOMER_REMOVED_SUCCESSFULLY, status = HttpStatusCode.Accepted)
        } else {
            call.respondText(text = CUSTOMER_NOT_FOUND, status = HttpStatusCode.NotFound)
        }
    }
}
