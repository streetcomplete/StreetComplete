import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File

/** Convert a markdown changelog to HTML. */
open class UpdateChangelogTask : DefaultTask() {

    @get:Input lateinit var sourceFile: String
    @get:Input lateinit var targetFile: String

    private val markdownFlavour = GFMFlavourDescriptor()

    @TaskAction fun run() {
        val sourceFile = File(sourceFile)
        val targetFile = File(targetFile)
        require(sourceFile.exists()) { "File ${sourceFile.absolutePath} does not exist." }

        val scriptName = UpdateChangelogTask::class.simpleName
        val comment = "<!-- Do not edit! This file was generated automatically from ${sourceFile.name} via $scriptName -->\n"
        targetFile.writeText(comment + convertToHtml(sourceFile.readText()))
    }

    private fun convertToHtml(markdown: String): String {
        val parsedTree = MarkdownParser(markdownFlavour).buildMarkdownTreeFromString(markdown)
        return HtmlGenerator(markdown, parsedTree, markdownFlavour).generateHtml()
            .replace("<body>", "")
            .replace("</body>", "")
            .replace("<h1>Changelog</h1>", "")
            .replace(Regex("(?<=[\\s(]|^)#(\\d+)")) { matchResult ->
                val issue = matchResult.groupValues[1]
                "<a href=\"https://github.com/streetcomplete/StreetComplete/issues/$issue\">#$issue</a>"
            }
            .replace(Regex("(?<=[\\s(]|^)@([a-zA-Z\\d-]+)")) { matchResult ->
                val contributor = matchResult.groupValues[1]
                "<a href=\"https://github.com/$contributor\">$contributor</a>"
            }
    }
}
