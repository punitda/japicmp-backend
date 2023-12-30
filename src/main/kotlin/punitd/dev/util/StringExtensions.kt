package punitd.dev.util

fun String.isValidPackageName(): Boolean {
    val packageRegex = """^([a-zA-Z0-9._-]+):([a-zA-Z0-9._-]+):([a-zA-Z0-9._-]+)$""".toRegex()
    return this.matches(packageRegex)
}

fun String.slugify(): String {
    // Replace non-alphanumeric characters with dashes
    val slug = this.replace(Regex("[^a-zA-Z0-9]"), "-")

    // Remove leading and trailing dashes
    return slug.trim('-').toLowerCase()
}