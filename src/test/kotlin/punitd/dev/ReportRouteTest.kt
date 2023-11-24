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

    private val validAarPackageNamesInRequestBodyForMaven = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com.stripe:stripe-android:18.0.0",
        newPackageName = "com.stripe:stripe-android:19.0.0",
        outputOnlyModifications = true,
        outputOnlyBinaryIncompatibleModifications = true,
    )

    private val validJarPackageNamesInRequestBodyForMaven = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com.squareup.okhttp3:okhttp:4.11.0",
        newPackageName = "com.squareup.okhttp3:okhttp:4.12.0",
        outputOnlyModifications = true,
        outputOnlyBinaryIncompatibleModifications = true,
    )

    private val unavailablePackageNamesInRequestBodyForMaven = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com.x:x:1.0.0",
        newPackageName = "com.x:x:2.0.0",
        outputOnlyModifications = false,
        outputOnlyBinaryIncompatibleModifications = false,
    )

    private val invalidPackageNamesInRequestBodyForMaven = GenerateReportByPackageNameRequestBody(
        oldPackageName = "com:okhttp",
        newPackageName = "com:okhttp",
        outputOnlyModifications = false,
        outputOnlyBinaryIncompatibleModifications = false,
    )

    private val validGenerateReportByFilesRequestBodyForAar = GenerateReportByFilesRequestBody(
        oldFileKeyName = "stripe-android-17.0.0.aar",
        newFileKeyName = "stripe-android-18.0.0.aar",
        outputOnlyModifications = true,
        outputOnlyBinaryIncompatibleModifications = true,
    )

    private val validGenerateReportByFilesRequestBodyForJar = GenerateReportByFilesRequestBody(
        oldFileKeyName = "okhttp-4.0.0.jar",
        newFileKeyName = "okhttp-4.11.0.jar",
        outputOnlyModifications = false,
        outputOnlyBinaryIncompatibleModifications = false,
    )

    @Test
    fun testGenerateReportForAar() = testApplication {
        val response = client.post("/report/maven") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByPackageNameRequestBody.serializer(),
                    validAarPackageNamesInRequestBodyForMaven
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
                    validJarPackageNamesInRequestBodyForMaven
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
                    unavailablePackageNamesInRequestBodyForMaven
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
                    invalidPackageNamesInRequestBodyForMaven
                )
            )
        }
        assertEquals(Constants.INVALID_PACKAGE_NAME, response.bodyAsText())
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testGenerateReportFileForAar() = testApplication {
        val response = client.post("/report/file") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByFilesRequestBody.serializer(),
                    validGenerateReportByFilesRequestBodyForAar
                )
            )
        }
        println("Body : ${response.bodyAsText()}")
    }

    @Test
    fun testGenerateReportFileForJar() = testApplication {
        val response = client.post("/report/file") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    GenerateReportByFilesRequestBody.serializer(),
                    validGenerateReportByFilesRequestBodyForJar
                )
            )
        }
        println("Body : ${response.bodyAsText()}")
    }
}