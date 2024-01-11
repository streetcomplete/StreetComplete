import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File

/**
 * Reads release notes from individual Markdown files, renders them as HTML
 * and merges everything into one JSON file.
 */
open class UpdateChangelogTask : DefaultTask() {

    @get:Input lateinit var sourceDir: String
    @get:Input lateinit var targetFile: String

    private val markdownFlavour = CommonMarkFlavourDescriptor()
    private val json = Json { prettyPrint = true }

    @TaskAction fun run() {
        val sourceDir = File(sourceDir)
        val targetFile = File(targetFile)
        require(sourceDir.exists()) { "Directory ${sourceDir.absolutePath} does not exist." }

        val changelogFiles = sourceDir.listFiles()!!
            .filter { it.extension == "md" }
            // .sortedBy { it.nameWithoutExtension.substring(1).toFloat() }
            .sortedWith(compareBy({ it.majorVersion }, { it.minorVersion }))
            .reversed()

        val jsonString = json.encodeToString(JsonObject(changelogFiles.associate {
            it.nameWithoutExtension to JsonPrimitive(getChangelogHtml(it))
        }))
        targetFile.writeText(jsonString)
    }

    private fun getChangelogHtml(file: File): String {
        val markdown = file.readText()
        val parsedTree = MarkdownParser(markdownFlavour).buildMarkdownTreeFromString(markdown)
        val html = HtmlGenerator(markdown, parsedTree, markdownFlavour).generateHtml()
            .replace("<body>", "")
            .replace("</body>", "")
            .replace(Regex("(?<=[\\s(]|^)#(\\d+)")) { matchResult ->
                val issue = matchResult.groupValues[1]
                "<a href=\"https://github.com/streetcomplete/StreetComplete/issues/$issue\">#$issue</a>"
            }
            .replace(Regex("(?<=[\\s(]|^)@([a-zA-Z\\d-]+)")) { matchResult ->
                val contributor = matchResult.groupValues[1]
                "<a href=\"https://github.com/$contributor\">$contributor</a>"
            }

        return html
    }
}

private val File.majorVersion get() = nameWithoutExtension.substring(1, nameWithoutExtension.indexOf('.')).toInt()
private val File.minorVersion get() = nameWithoutExtension.substring(nameWithoutExtension.indexOf('.') + 1).toInt()
