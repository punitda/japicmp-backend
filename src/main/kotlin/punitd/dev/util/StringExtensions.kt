package punitd.dev.util

fun String.isValidPackageName(): Boolean {
    val packageRegex = """^([a-zA-Z0-9._-]+):([a-zA-Z0-9._-]+):([\d.]+)$""".toRegex()
    return this.matches(packageRegex)
}
