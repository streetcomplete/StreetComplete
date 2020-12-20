import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.net.URL

open class UpdateNsiPresetsTask : DefaultTask() {
    @get:Input var targetDir: String? = null

    @TaskAction
    fun run() {
        val targetDir = targetDir ?: return

        val presetsFile = File("$targetDir/presets.json")
        val presetsUrl = URL("https://raw.githubusercontent.com/osmlab/name-suggestion-index/main/dist/presets/nsi-id-presets.json")
        presetsUrl.openStream().use { input ->
            FileOutputStream(presetsFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
