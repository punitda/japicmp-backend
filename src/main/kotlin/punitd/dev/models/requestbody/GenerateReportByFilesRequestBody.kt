package punitd.dev.models.requestbody

import kotlinx.serialization.Serializable

@Serializable
data class GenerateReportByFilesRequestBody(
    val oldFileKeyName: String,
    val oldVersion: String,
    val newFileKeyName: String,
    val newVersion: String,
)

