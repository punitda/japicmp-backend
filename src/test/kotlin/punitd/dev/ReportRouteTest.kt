package punitd.dev

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Test
import punitd.dev.models.requestbody.GenerateReportByFilesRequestBody
import punitd.dev.models.requestbody.GenerateReportByPackageNameRequestBody
import punitd.dev.util.Constants
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReportRouteTest {

    private val validAarPackageNamesInRequestBody = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com.stripe:stripe-android:18.0.0",
        newPackageName = "com.stripe:stripe-android:19.0.0"
    )

    private val validJarPackageNamesInRequestBody = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com.squareup.okhttp3:okhttp:4.11.0",
        newPackageName = "com.squareup.okhttp3:okhttp:4.12.0"
    )

    private val unavailablePackageNamesInRequestBody = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com.x:x:1.0.0",
        newPackageName = "com.x:x:2.0.0"
    )

    private val invalidPackageNamesInRequestBody = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com:okhttp",
        newPackageName = "com:okhttp"
    )

    @Test
    fun testGenerateReportForAar() = testApplication {
        val response = client.post("/report/maven") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByPackageNameRequestBody.serializer(),
                    validAarPackageNamesInRequestBody
                )
            )
        }
        assertNotNull(response.bodyAsText())
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun testGenerateReportForJar() = testApplication {
        val response = client.post("/report/maven") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByPackageNameRequestBody.serializer(),
                    validJarPackageNamesInRequestBody
                )
            )
        }
        assertNotNull(response.bodyAsText())
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun testGenerateReportWithUnavailablePackageNames() = testApplication {
        val response = client.post("/report/maven") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByPackageNameRequestBody.serializer(),
                    unavailablePackageNamesInRequestBody
                )
            )
        }
        assertEquals(Constants.PACKAGE_NAME_NOT_FOUND_ON_MAVEN, response.bodyAsText())
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testGenerateReportWithInvalidPackageNames() = testApplication {
        val response = client.post("/report/maven") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByPackageNameRequestBody.serializer(),
                    invalidPackageNamesInRequestBody
                )
            )
        }
        assertEquals(Constants.INVALID_PACKAGE_NAME, response.bodyAsText())
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testGenerateReportFileForAar() = testApplication {
        val generateReportByFilesRequestBody = GenerateReportByFilesRequestBody(
            oldFileKeyName = "stripe-android-17.0.0.aar",
            oldVersion = "17.0.0",
            newFileKeyName = "stripe-android-18.0.0.aar",
            newVersion = "18.0.0"
        )
        val resposne = client.post("/report/file") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByFilesRequestBody.serializer(),
                    generateReportByFilesRequestBody
                )
            )
        }
        println("Body : ${resposne.bodyAsText()}")
    }

    @Test
    fun testGenerateReportFileForJar() = testApplication {
        val generateReportByFilesRequestBody = GenerateReportByFilesRequestBody(
            oldFileKeyName = "okhttp-4.0.0.jar",
            oldVersion = "4.0.0",
            newFileKeyName = "okhttp-4.11.0.jar",
            newVersion = "4.11.0"
        )
        val resposne = client.post("/report/file") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByFilesRequestBody.serializer(),
                    generateReportByFilesRequestBody
                )
            )
        }
        println("Body : ${resposne.bodyAsText()}")
    }
}