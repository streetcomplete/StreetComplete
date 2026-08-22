import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File

/** Convert a Markdown changelog to HTML. */
open class UpdateChangelogTask : DefaultTask() {

    @get:InputFile lateinit var sourceFile: File
    @get:OutputFile lateinit var targetFile: File

    private val markdownFlavour = GFMFlavourDescriptor()

    @TaskAction fun run() {
        require(sourceFile.exists()) { "File ${sourceFile.absolutePath} does not exist." }

        val scriptName = UpdateChangelogTask::class.simpleName
        val comment = "<!-- Do not edit! This file was generated automatically from ${sourceFile.name} via $scriptName -->\n"
        val html = convertToHtml(sourceFile.readText().replace("\r\n", "\n"))
        targetFile.writeText(comment + html)
    }

    private fun convertToHtml(markdown: String): String {
        val substringsToRemove = listOf("</?body>", "<h1>Changelog</h1>")
        val tagsToAddNewLineAfter = listOf("</h[1-6]>", "</?ul>", "</?ol>", "</li>", "</p>")

        val parsedTree = MarkdownParser(markdownFlavour).buildMarkdownTreeFromString(markdown)
        return HtmlGenerator(markdown, parsedTree, markdownFlavour).generateHtml()
            .replace(Regex(substringsToRemove.joinToString("|")), "")
            .replace(Regex("(?<=[\\s(]|^)#(\\d+)")) { matchResult ->
                val issue = matchResult.groupValues[1]
                "<a href=\"https://github.com/streetcomplete/StreetComplete/issues/$issue\">#$issue</a>"
            }
            .replace(Regex("(?<=[\\s(]|^)@([a-zA-Z\\d-]+)")) { matchResult ->
                val contributor = matchResult.groupValues[1]
                "<a href=\"https://github.com/$contributor\">$contributor</a>"
            }
            .replace(Regex(tagsToAddNewLineAfter.joinToString("|")), "$0\n")
    }
}
