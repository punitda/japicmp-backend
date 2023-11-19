package punitd.dev.util

data class EnvConfig(
    val AWS_ACCESS_KEY: String,
    val AWS_SECRET_KEY: String,
    val AWS_SERVICE_ENDPOINT_R2: String,
    val AWS_SIGNING_REGION: String,
    val BUCKET_NAME: String,
)