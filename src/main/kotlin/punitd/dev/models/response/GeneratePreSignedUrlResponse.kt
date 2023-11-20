package punitd.dev.models.response

import kotlinx.serialization.Serializable

@Serializable()
data class GeneratePreSignedUrlResponse(
    val preSignedUrl: String,
    val objectKey: String,
)
