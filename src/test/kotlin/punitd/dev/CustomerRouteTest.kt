package punitd.dev

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json.Default.encodeToString
import org.junit.After
import org.junit.Before
import org.junit.Test
import punitd.dev.models.Customer
import punitd.dev.models.customerStorage
import punitd.dev.routes.*
import kotlin.test.assertEquals

class CustomerRouteTest {

    private val customer1 = Customer(
        id = "1",
        firstName = "Kendall Patel",
        lastName = "Allison Goodman",
        email = "gena.bond@example.com"
    )

    private val customer2 = Customer(
        id = "2",
        firstName = "Goldie Golden",
        lastName = "Earline Solomon",
        email = "shelia.todd@example.com"
    )

    @Before
    fun setup() {
        customerStorage.addAll(listOf(customer1, customer2))
    }

    @After
    fun clear() {
        customerStorage.clear()
    }


    @Test
    fun testGetCustomer() = testApplication {
        val response = client.get("/customer")
        assertEquals(
            encodeToString(ListSerializer(Customer.serializer()), customerStorage),
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetCustomerNotFound() = testApplication {
        customerStorage.clear()
        val response = client.get("/customer")
        assertEquals(
            NO_CUSTOMERS_FOUND,
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetCustomerById() = testApplication {
        val response = client.get("/customer/${customer1.id}")
        assertEquals(
            encodeToString(Customer.serializer(), customer1),
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testGetCustomerByIncorrectId() = testApplication {
        val id = "1000"
        val response = client.get("/customer/${id}")
        assertEquals(
            "$NO_CUSTOMER_WITH_ID $id",
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testCreateCustomer() = testApplication {
        customerStorage.clear()
        val response = client.post("/customer") {
            contentType(ContentType.Application.Json)
            setBody(encodeToString(Customer.serializer(), customer1))
        }
        assertEquals(
            CUSTOMER_STORED_SUCCESSFULLY,
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun testCreateCustomerWithInvalidRequestBody() = testApplication {
        customerStorage.clear()
        val response = client.post("/customer") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(
            INVALID_REQUEST_BODY,
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testDeleteCustomer() = testApplication {
        val response = client.delete("/customer/${customer1.id}")
        assertEquals(
            CUSTOMER_REMOVED_SUCCESSFULLY,
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.Accepted, response.status)
    }

    @Test
    fun testDeleteCustomerWithInvalidCustomer() = testApplication {
        val invalidCustomerId = "1000"
        val response = client.delete("/customer/${invalidCustomerId}")
        assertEquals(
            CUSTOMER_NOT_FOUND,
            response.bodyAsText()
        )
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testDeleteCustomerWithNoIdPassed() = testApplication {
        val response = client.delete("/customer")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}