package punitd.dev.models.requestbody

import kotlinx.serialization.Serializable

@Serializable
data class GeneratePreSignedUrlRequestBody(
    val fileName: String,
    val fileType: String,
)