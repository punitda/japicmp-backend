package punitd.dev.manager

import japicmp.cmp.JApiCmpArchive
import japicmp.cmp.JarArchiveComparator
import japicmp.cmp.JarArchiveComparatorOptions
import japicmp.config.Options
import japicmp.model.AccessModifier
import japicmp.model.JApiClass
import japicmp.output.xml.XmlOutputGenerator
import japicmp.output.xml.XmlOutputGeneratorOptions
import japicmp.util.Optional
import java.io.File

object ReportGenerator {

    fun generateReport(
        oldArtifactFile: File,
        oldVersion: String,
        newArtifactFile: File,
        newVersion: String
    ): List<File> {
        val oldArchive = JApiCmpArchive(oldArtifactFile, oldVersion)
        val newArchive = JApiCmpArchive(newArtifactFile, newVersion)
        val options = Options.newDefault().apply {
            isOutputOnlyModifications = true
            isOutputOnlyBinaryIncompatibleModifications = true
            setIgnoreMissingClasses(true)
            oldArchives = listOf(oldArchive)
            newArchives = listOf(newArchive)
            accessModifier = AccessModifier.PUBLIC
            htmlOutputFile = Optional.of("build/report.html")
        }

        val jApiClasses = compareArchives(
            oldArchive = oldArchive,
            newArchive = newArchive,
            options = options
        )
        return generateHTMLReport(jApiClasses, options)
    }

    private fun compareArchives(
        oldArchive: JApiCmpArchive,
        newArchive: JApiCmpArchive,
        options: Options
    ): List<JApiClass> {
        val jarArchiveComparatorOptions = JarArchiveComparatorOptions.of(options)
        val comparator = JarArchiveComparator(jarArchiveComparatorOptions)
        return comparator.compare(oldArchive, newArchive)
    }

    private fun generateHTMLReport(jApiClasses: List<JApiClass>, options: Options): List<File> {
        val xmlOutputGeneratorOptions = XmlOutputGeneratorOptions()
        val generator = XmlOutputGenerator(jApiClasses, options, xmlOutputGeneratorOptions)
        val xmlOutput = generator.generate()
        return XmlOutputGenerator.writeToFiles(options, xmlOutput)
    }

}