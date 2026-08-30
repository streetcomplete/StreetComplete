import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.collections.orEmpty

open class CopyStringsTask : DefaultTask() {
    @get:InputDirectory lateinit var sourceDir: File
    @get:OutputDirectory lateinit var targetDir: File

    @TaskAction
    fun run() {
        val stringRegex = Regex(
            pattern = "<string name=\"([a-zA-Z0-9_]+)\">(.*?)</string>",
            options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
        )
        for (dir in sourceDir.listFiles().orEmpty()) {
            if (dir.isDirectory) {
                val stringsFile = File(dir, "strings.xml")
                if (stringsFile.exists()) {
                    val stringsFileText = stringsFile.readText()
                    val newStringsFileText = stringRegex.replace(stringsFileText) {
                        val key = it.groupValues[1]
                        val value = it.groupValues[2]
                        "<string name=\"$key\">\"${value.escapeXml().replace("\"", "\\\"")}\"</string>"
                    }

                    val targetStringsDir = targetDir.resolve(dir.name)
                    val targetStringsFile = targetStringsDir.resolve(stringsFile.name)

                    targetStringsDir.mkdirs()
                    targetStringsFile.writeText(newStringsFileText)
                }
            }
        }
    }
}
