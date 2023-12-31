package punitd.dev.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import punitd.dev.models.response.ArtifactResult
import punitd.dev.models.response.MavenSearchResponse
import punitd.dev.util.FileUtil
import java.io.File
import kotlin.io.path.absolute

interface MavenRepository {
    suspend fun searchPackages(oldPackageName: String, newPackageName: String): List<MavenSearchResponse?>
    suspend fun downloadFiles(
        oldArtifactResult: ArtifactResult,
        newArtifactResult: ArtifactResult,
        dirPath: String,
    ): List<File?>
}

class MavenRepositoryImpl(private val client: HttpClient) : MavenRepository {

    override suspend fun searchPackages(oldPackageName: String, newPackageName: String): List<MavenSearchResponse?> =
        coroutineScope {
            val oldPackageSearchResponse = async { search(oldPackageName) }
            val newPackageSearchResponse = async { search(newPackageName) }
            return@coroutineScope awaitAll<MavenSearchResponse?>(
                oldPackageSearchResponse,
                newPackageSearchResponse
            )
        }

    override suspend fun downloadFiles(
        oldArtifactResult: ArtifactResult,
        newArtifactResult: ArtifactResult,
        dirPath: String,
    ): List<File?> =
        coroutineScope {
            val oldFile = async { downloadFile(oldArtifactResult, dirPath) }
            val newFile = async { downloadFile(newArtifactResult, dirPath) }
            return@coroutineScope awaitAll<File?>(oldFile, newFile)
        }

    private suspend fun search(packageName: String): MavenSearchResponse? {
        return runCatching {
            val (groupId, artifactId, version) = packageName.split(":")
            val query = "g:${groupId}+AND+a:${artifactId}+AND+v:${version}"
            val searchUrl = "https://search.maven.org/solrsearch/select?q=$query&wt=json"
            val result = client.get(searchUrl)
            return result.body<MavenSearchResponse>()
        }.getOrNull()
    }

    private suspend fun downloadFile(artifactResult: ArtifactResult, dirPath: String): File? {
        return runCatching {
            // Local file path where we need to copy jar/aar
            val file = File(
                dirPath,
                "${artifactResult.artifactId}-${artifactResult.version}.${artifactResult.format}"
            )

            val filePath = artifactResult.getFilePath()
            val mavenRemoteFilePathUrl = "https://search.maven.org/remotecontent?filepath=${filePath}"
            val fileResult = client.get(mavenRemoteFilePathUrl)

            val channel: ByteReadChannel = fileResult.body()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    file.appendBytes(bytes)
                }
            }
            return file
        }.getOrNull()
    }

}