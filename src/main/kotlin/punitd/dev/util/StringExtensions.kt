package punitd.dev.util

fun String.isValidPackageName(): Boolean {
    val packageRegex = """^([a-zA-Z._-]+):([a-zA-Z._-]+):([\d.]+)$""".toRegex()
    return this.matches(packageRegex)
}
