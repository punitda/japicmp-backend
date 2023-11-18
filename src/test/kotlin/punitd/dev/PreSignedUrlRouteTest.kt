package punitd.dev

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Test
import punitd.dev.models.requestbody.GeneratePreSignedUrlRequestBody
import punitd.dev.models.response.GeneratePreSignedUrlResponse
import punitd.dev.util.Constants
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PreSignedUrlRouteTest {

    private val generatePreSignedUrlRequestBody = GeneratePreSignedUrlRequestBody(
        fileName = "stripe-android",
        fileType = "application/java-archive"
    )

    @Test
    fun testPreSignedUrl() = testApplication {
        val response = client.post("/presigned") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GeneratePreSignedUrlRequestBody.serializer(),
                    generatePreSignedUrlRequestBody
                )
            )
        }

        assertNotNull(response.bodyAsText())
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun testPreSignedUrlWithInvalidRequestBody() = testApplication {
        val response = client.post("/presigned") {
            contentType(ContentType.Application.Json)
        }

        assertEquals(response.bodyAsText(), Constants.MISSING_FIELDS_IN_REQUEST_BODY)
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}