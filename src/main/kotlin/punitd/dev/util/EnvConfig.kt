package punitd.dev.util

object EnvConfig {
    val BUCKET_NAME: String = System.getenv("BUCKET_NAME")
    val AWS_ACCESS_KEY: String = System.getenv("AWS_ACCESS_KEY")
    val AWS_SECRET_KEY: String = System.getenv("AWS_SECRET_KEY")
    val AWS_SERVICE_ENDPOINT_R2: String = System.getenv("AWS_SERVICE_ENDPOINT_R2")
    val AWS_SIGNING_REGION: String = System.getenv("AWS_SIGNING_REGION")
}