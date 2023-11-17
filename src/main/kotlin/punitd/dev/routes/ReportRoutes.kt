package punitd.dev.routes

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import japicmp.cmp.JApiCmpArchive
import japicmp.config.Options
import japicmp.model.AccessModifier
import japicmp.util.Optional
import kotlinx.serialization.json.Json
import punitd.dev.manager.FileManager
import punitd.dev.manager.ReportGenerator
import punitd.dev.models.requestbody.GenerateReportByPackageNameRequestBody
import punitd.dev.repository.MavenRepository
import punitd.dev.util.Constants
import punitd.dev.util.MissingFieldException
import punitd.dev.util.isValidPackageName
import java.io.File

@Resource("/report")
private class ReportPath {
    @Resource("/maven")
    class Maven(val parent: ReportPath = ReportPath())

    @Resource("/file")
    class File(val parent: ReportPath = ReportPath())
}


fun Application.reportRoutes() {
    routing {
        createReportMaven()
    }
}

fun Route.createReportMaven() {
    val client = HttpClient(OkHttp) {
        // Install ContentNegotiation plugin
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }
    post<ReportPath.Maven> {
        val requestBody = runCatching { call.receiveNullable<GenerateReportByPackageNameRequestBody>() }.getOrNull()
            ?: throw MissingFieldException("Missing fields in request body")

        // Package name validation
        val (oldPackageName, newPackageName) = requestBody
        if (!oldPackageName.isValidPackageName() || !newPackageName.isValidPackageName()) {
            return@post call.respondText(
                text = Constants.INVALID_PACKAGE_NAME,
                status = HttpStatusCode.BadRequest
            )
        }

        // Search packages on Maven
        val mavenRepository = MavenRepository(client)
        val searchResults = mavenRepository.searchPackages(
            oldPackageName = oldPackageName,
            newPackageName = newPackageName
        )
        if (searchResults.any { it == null || it.response.resultCount == 0 || it.response.results.isEmpty() }) {
            return@post call.respondText(
                text = Constants.PACKAGE_NAME_NOT_FOUND_ON_MAVEN,
                status = HttpStatusCode.BadRequest
            )
        }

        // Download files from Maven
        val (oldPackageSearchResult, newPackageSearchResult) = searchResults
        val oldArtifactResult = oldPackageSearchResult?.artifactResult()!!
        val newArtifactResult = newPackageSearchResult?.artifactResult()!!

        val fileResults = mavenRepository.downloadFiles(
            oldArtifactResult = oldArtifactResult,
            newArtifactResult = newArtifactResult
        )

        if (fileResults.any { it == null } || fileResults.size != 2) {
            return@post call.respondText(
                text = "Unable to download files from Maven",
                status = HttpStatusCode.BadRequest
            )
        }

        // Generate Report
        val (_, _, oldVersion) = oldPackageName.split(":")
        val (_, _, newVersion) = newPackageName.split(":")
        val artifacts = FileManager.getArtifacts(
            oldArtifact = fileResults.first()!!,
            newArtifact = fileResults.last()!!,
            isAar = oldArtifactResult.isAar(),
        )
        val (oldArtifact, newArtifact) = artifacts
        if (oldArtifact == null || newArtifact == null) {
            return@post call.respondText(
                text = "Unable to find artifact files",
                status = HttpStatusCode.InternalServerError
            )
        }

        val outputFiles = ReportGenerator.generateReport(
            oldArtifactFile = oldArtifact,
            oldVersion = oldVersion,
            newArtifactFile = newArtifact,
            newVersion = newVersion
        )

        // Send HTML Report in response
        val file = outputFiles.first()
        return@post call.respondFile(file)
    }
}