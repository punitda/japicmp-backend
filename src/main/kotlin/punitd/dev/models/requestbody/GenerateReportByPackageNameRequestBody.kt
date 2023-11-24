package punitd.dev.models.requestbody

import kotlinx.serialization.Serializable

@Serializable
data class GenerateReportByPackageNameRequestBody(
    val oldPackageName: String,
    val newPackageName: String,
    val outputOnlyModifications: Boolean,
    val outputOnlyBinaryIncompatibleModifications: Boolean,
)