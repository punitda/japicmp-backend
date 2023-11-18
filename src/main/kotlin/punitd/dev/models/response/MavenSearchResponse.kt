@file:OptIn(ExperimentalSerializationApi::class)

package punitd.dev.models.response

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MavenSearchResponse(
    val response: Response,
) {
    fun artifactResult(): ArtifactResult? {
        return response.results.firstOrNull()
    }
}

@Serializable
data class Response(
    @JsonNames("numFound") val resultCount: Int = 0,
    @JsonNames("docs") val results: List<ArtifactResult> = emptyList()
)

@Serializable
data class ArtifactResult(
    val id: String,
    @JsonNames("g") val groupId: String,
    @JsonNames("a") val artifactId: String,
    @JsonNames("v") val version: String,
    @JsonNames("p") val format: String,
) {
    fun getFilePath(): String {
        val groupPath = groupId.replace(".", "/")
        val artifactPath = artifactId.replace(".", "/")
        val version = version
        val isAar = format == "aar"
        val fileExtension = if (isAar) "aar" else "jar"
        val artifactFileName = "${artifactId}-${version}.${fileExtension}"
        return "${groupPath}/${artifactPath}/${version}/${artifactFileName}"
    }

    fun isAar() : Boolean {
        return format == "aar"
    }
}