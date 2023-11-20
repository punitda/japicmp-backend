package punitd.dev.routes

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import punitd.dev.models.requestbody.GeneratePreSignedUrlRequestBody
import punitd.dev.models.response.GeneratePreSignedUrlResponse
import punitd.dev.util.Constants
import punitd.dev.util.EnvConfig
import punitd.dev.util.MissingFieldException
import punitd.dev.util.slugify
import java.util.*


@Resource("/presigned")
private class PreSignedUrlPath {}

fun Application.preSignedUrlRoute() {
    routing {
        generatePreSignedUrl()
    }
}

fun Route.generatePreSignedUrl() {
    val envConfig by inject<EnvConfig>()
    val s3 by inject<AmazonS3>()

    post<PreSignedUrlPath> {
        val requestBody = runCatching { call.receiveNullable<GeneratePreSignedUrlRequestBody>() }.getOrNull()
            ?: throw MissingFieldException(Constants.MISSING_FIELDS_IN_REQUEST_BODY)

        runCatching {
            val (fileName, fileType) = requestBody
            val expirationTime = Date().time + 1000 * 2 * 60 // 2 min expiration
            val bucket = envConfig.BUCKET_NAME
            val objectKey = "${Date().time.toString().slugify()}-${fileName.slugify()}"
            val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucket, objectKey)
                .withExpiration(Date(expirationTime))
                .withContentType(fileType)
                .withMethod(HttpMethod.PUT)

            val result = s3.generatePresignedUrl(generatePresignedUrlRequest)

            call.response.status(HttpStatusCode.Created)
            return@post call.respond(
                GeneratePreSignedUrlResponse(
                    preSignedUrl = result.toURI().toString(),
                    objectKey = objectKey
                ),
            )
        }.getOrElse { exception ->
            return@post call.respondText(
                text = exception.message ?: Constants.PRESIGNED_URL_GENERATE_ERROR_MSG,
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}
