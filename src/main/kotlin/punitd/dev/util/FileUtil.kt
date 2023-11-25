package punitd.dev.util

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtil {

    fun createOutputDirectoryForRequest(requestId: String): Path {
        val dirPath = Paths.get("build/${requestId}")
        if (!Files.exists(dirPath)) {
            Files.createDirectory(dirPath)
        }
        return dirPath
    }

    fun deleteOutputDirectoryForRequest(requestId: String) {
        val outputDirectory = File("build/${requestId}")
        if (outputDirectory.exists()) {
            outputDirectory.deleteRecursively()
        }
    }
}