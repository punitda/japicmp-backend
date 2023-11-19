package punitd.dev.di

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import punitd.dev.util.EnvConfig
import kotlin.math.sin

val appModule = module {
    single<EnvConfig>(createdAtStart = true) {
        ConfigLoaderBuilder.default()
            .addResourceSource("/env.json")
            .build()
            .loadConfigOrThrow<EnvConfig>()
    }

    single<AmazonS3> {
        val envConfig = get<EnvConfig>()
        val credentials = BasicAWSCredentials(envConfig.AWS_ACCESS_KEY, envConfig.AWS_SECRET_KEY)
        AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration(
                    envConfig.AWS_SERVICE_ENDPOINT_R2,
                    envConfig.AWS_SIGNING_REGION
                )
            ).build()
    }

    single<HttpClient> {
        HttpClient(OkHttp) {
            // Install ContentNegotiation plugin
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }
}