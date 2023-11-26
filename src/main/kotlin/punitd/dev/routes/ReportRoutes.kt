package punitd.dev.routes

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import punitd.dev.manager.FileManager
import punitd.dev.manager.FileManager.aarToClassesJar
import punitd.dev.manager.ReportGenerator
import punitd.dev.models.requestbody.GenerateReportByFilesRequestBody
import punitd.dev.models.requestbody.GenerateReportByPackageNameRequestBody
import punitd.dev.models.response.GeneratePreSignedUrlResponse
import punitd.dev.repository.MavenRepository
import punitd.dev.repository.StorageRepository
import punitd.dev.util.Constants
import punitd.dev.util.FileUtil
import punitd.dev.util.MissingFieldException
import punitd.dev.util.isValidPackageName
import java.io.File
import kotlin.io.path.absolute

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
        createReportFile()
    }
}

fun Route.createReportMaven() {
    val mavenRepository by inject<MavenRepository>()
    val storageRepository by inject<StorageRepository>()
    post<ReportPath.Maven> {
        val requestBody = runCatching { call.receiveNullable<GenerateReportByPackageNameRequestBody>() }.getOrNull()
            ?: throw MissingFieldException("Missing fields in request body")

        // Package name validation
        val (oldPackageName, newPackageName, outputOnlyModifications, outputOnlyBinaryIncompatibleModifications) = requestBody
        val requestId = call.response.headers[HttpHeaders.XRequestId]

        if (!oldPackageName.isValidPackageName() || !newPackageName.isValidPackageName()) {
            return@post call.respondText(
                text = Constants.INVALID_PACKAGE_NAME,
                status = HttpStatusCode.BadRequest
            )
        }

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

        val dirPath = FileUtil.createOutputDirectoryForRequest(requestId ?: oldPackageName).absolute().toString()
        val fileResults = mavenRepository.downloadFiles(
            oldArtifactResult = oldArtifactResult,
            newArtifactResult = newArtifactResult,
            dirPath = dirPath,
        )

        if (fileResults.any { it == null } || fileResults.size != 2) {
            return@post call.respondText(
                text = "Unable to download files from Maven. Please try again",
                status = HttpStatusCode.InternalServerError
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
                text = "Unable to find artifact files. Please try again",
                status = HttpStatusCode.InternalServerError
            )
        }

        val outputFiles = ReportGenerator.generateReport(
            oldArtifactFile = oldArtifact,
            oldVersion = oldVersion,
            newArtifactFile = newArtifact,
            newVersion = newVersion,
            outputReportPath = "build/${requestId}/report.html",
            outputOnlyModifications = outputOnlyModifications,
            outputOnlyBinaryIncompatibleModifications = outputOnlyBinaryIncompatibleModifications,
        )


        // Upload report to object storage
        val file = outputFiles.first()
        call.sendReport(
            storageRepository = storageRepository,
            file = file,
            requestId = requestId ?: oldPackageName
        )
    }
}

fun Route.createReportFile() {
    val storageRepository by inject<StorageRepository>()
    post<ReportPath.File> {
        val requestBody = runCatching { call.receiveNullable<GenerateReportByFilesRequestBody>() }.getOrNull()
            ?: throw MissingFieldException("Missing fields in request body")

        val (oldFileKeyName, newFileKeyName, outputOnlyModifications, outputOnlyBinaryIncompatibleModifications) = requestBody
        val requestId = call.response.headers[HttpHeaders.XRequestId]

        runCatching {
            val dirPath = FileUtil.createOutputDirectoryForRequest(requestId ?: oldFileKeyName)
            val oldFilePath = "${dirPath.absolute()}/${oldFileKeyName}"
            val newFilePath = "${dirPath.absolute()}/${newFileKeyName}"
            storageRepository.downloadFile(oldFileKeyName, oldFilePath)
            storageRepository.downloadFile(newFileKeyName, newFilePath)

            val isAar = oldFileKeyName.contains("aar") && newFileKeyName.contains("aar")

            val outputFiles = ReportGenerator.generateReport(
                oldArtifactFile = if (isAar) aarToClassesJar(File(oldFilePath))!! else File(oldFilePath),
                newArtifactFile = if (isAar) aarToClassesJar(File(newFilePath))!! else File(newFilePath),
                outputReportPath = "build/${requestId}/report.html",
                outputOnlyModifications = outputOnlyModifications,
                outputOnlyBinaryIncompatibleModifications = outputOnlyBinaryIncompatibleModifications,
            )


            // Upload report to object storage
            val file = outputFiles.first()
            call.sendReport(
                storageRepository = storageRepository,
                file = file,
                requestId = requestId ?: oldFileKeyName
            )
        }.getOrElse {
            return@post call.respondText(
                text = "Unable to generate report",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}

suspend fun ApplicationCall.sendReport(storageRepository: StorageRepository, file: File, requestId: String) {
    try {
        // Upload report to object storage
        val objectKey = "${requestId}-report"
        storageRepository.uploadFile(key = objectKey, file = file)

        // Generate presigned url for client to access report
        val preSignedUrl = storageRepository.generatePresignedUrlRequestForReport(objectKey)

        this.response.status(HttpStatusCode.Created)
        this.respond(
            GeneratePreSignedUrlResponse(
                preSignedUrl = preSignedUrl,
                objectKey = objectKey
            ),
        )
    } catch (e: Exception) {
        // Do nothing
    } finally {
        FileUtil.deleteOutputDirectoryForRequest(requestId)
    }
}


