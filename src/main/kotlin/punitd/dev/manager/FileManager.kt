package punitd.dev.manager

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileManager {
    fun aarToClassesJar(aarFile: File): File? {
        val zipFile = File(aarFile.parent, "${aarFile.nameWithoutExtension}.zip")

        // Renaming the .aar file to .zip
        aarFile.renameTo(zipFile)

        // Unzipping the .zip file
        val destDir = File(aarFile.parent, aarFile.nameWithoutExtension)
        destDir.mkdir()

        val buffer = ByteArray(1024)
        val zis = ZipInputStream(zipFile.inputStream())
        var entry: ZipEntry? = zis.nextEntry
        while (entry != null) {
            val fileName = entry.name
            val newFile = File(destDir, fileName)
            if (!entry.isDirectory) {
                newFile.parentFile?.mkdirs()
                newFile.outputStream().use { fos ->
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
        zis.close()

        // Finding classes.jar in the unzipped folder
        val classesJarFile = File(destDir, "classes.jar")
        return if (classesJarFile.exists()) {
            classesJarFile
        } else {
            null
        }
    }

    fun getArtifacts(oldArtifact: File, newArtifact: File, isAar: Boolean): Pair<File?, File?> {
        return if (isAar) {
            Pair(
                aarToClassesJar(oldArtifact),
                aarToClassesJar(newArtifact)
            )
        } else {
            Pair(oldArtifact, newArtifact)
        }
    }

}