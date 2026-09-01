import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI

/** Pulls the newest map style from the maplibre-streetcomplete-style repository */
open class UpdateMapStyleTask : DefaultTask() {

    @get:OutputDirectory lateinit var targetDir: File
    @get:Input lateinit var mapStyleBranch: String
    @get:Input lateinit var apiKey: String

    @TaskAction fun run() {
        require(targetDir.exists()) { "Directory ${targetDir.absolutePath} does not exist." }

        val urls = listOf(
            "https://raw.githubusercontent.com/streetcomplete/maplibre-streetcomplete-style/master/demo/streetcomplete.json",
            "https://raw.githubusercontent.com/streetcomplete/maplibre-streetcomplete-style/master/demo/streetcomplete-night.json",
        ).map { URI(it).toURL() }

        for (url in urls) {
            val fileName = File(url.path).name
            val targetFile = targetDir.resolve(fileName)

            val fileContent = url.readText()
                .normalizeLineEndings()
                .replaceAccessToken(apiKey)
                .replaceGlyphs()
                .replaceSprites()

            targetFile.writeText(fileContent)
        }
    }
}

private fun String.normalizeLineEndings() = this.replace("\r\n", "\n")

private fun String.replaceAccessToken(apiKey: String): String =
    replace(Regex("\\?access-token=[0-9A-Za-z+/=]*"), "?access-token=$apiKey")

private fun String.replaceGlyphs(): String =
    replace(Regex("https://api.jawg.io/glyphs"), "asset://map_theme/glyphs")

private fun String.replaceSprites(): String =
    replace(Regex("https://streetcomplete.app/map-jawg/sprites"), "asset://map_theme/sprites")
