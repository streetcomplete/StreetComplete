import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

open class UpdateNsiPresetsTask : DefaultTask() {
    @get:Input var targetDir: String? = null
    @get:Input var version: String? = null

    @TaskAction
    fun run() {
        val targetDir = targetDir ?: return
        val version = version ?: return

        val presetsFile = File("$targetDir/presets.json")
        val presetsUrl = URL("https://raw.githubusercontent.com/osmlab/name-suggestion-index/$version/dist/presets/nsi-id-presets.min.json")
        val nsiPresetsJson = Parser.default().parse(presetsUrl.openStream()) as JsonObject
        // NSI uses (atm) a slightly different format than the normal presets: The presets are in
        // a sub-object called "presets"
        val presets = nsiPresetsJson.obj("presets")!!
        // since we already read the JSON and it is so large, let's drop some properties that are
        // (currently) not used, to make it a bit smaller: icon, imageURL
        for (preset in presets.values.filterIsInstance<JsonObject>()) {
            preset.remove("icon")
            preset.remove("imageURL")
        }
        presetsFile.writeText(presets.toJsonString())
    }
}
