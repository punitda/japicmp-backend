package punitd.dev.repository

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3Object
import punitd.dev.util.EnvConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

interface StorageRepository {
    fun uploadFile(key: String, file: File)
    fun generatePresignedUrlRequestForReport(key: String): String
    fun downloadFile(key: String, filePath: String)
}

class StorageRepositoryImpl(
    private val s3: AmazonS3,
    private val envConfig: EnvConfig
) : StorageRepository {

    override fun uploadFile(key: String, file: File) {
        val bucket = envConfig.REPORT_BUCKET_NAME
        val putObjectRequest = PutObjectRequest(bucket, key, file)
        s3.putObject(putObjectRequest)
    }

    override fun downloadFile(key: String, filePath: String) {
        val s3Object = s3.getObject(envConfig.BUCKET_NAME, key)
        convertS3ObjectToFile(s3Object, filePath)
    }

    override fun generatePresignedUrlRequestForReport(key: String): String {
        val bucket = envConfig.REPORT_BUCKET_NAME
        val expirationTime = Date().time + 1000 * 2 * 60 // 2 min expiration

        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucket, key)
            .withExpiration(Date(expirationTime))
            .withMethod(HttpMethod.GET)

        val result = s3.generatePresignedUrl(generatePresignedUrlRequest)
        return result.toURI().toString()
    }

    private fun convertS3ObjectToFile(s3Object: S3Object, localFilePath: String) {
        try {
            s3Object.objectContent.use { inputStream ->
                FileOutputStream(localFilePath).use { fileOutputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}