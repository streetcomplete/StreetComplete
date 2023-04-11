
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL

/** Download all the brand logos referred to in the name suggestion index.
 *
 *  Unused, it is just too much to deliver them with the app. */
open class DownloadBrandLogosTask : DefaultTask() {
    @get:Input var targetDir: String? = null
    @get:Input var version: String? = null

    @TaskAction fun run() {
        val targetDir = targetDir ?: return
        val version = version ?: return

        val presetsUrl = URL("https://raw.githubusercontent.com/osmlab/name-suggestion-index/$version/dist/presets/nsi-id-presets.min.json")
        val nsiPresetsJson = Parser.default().parse(presetsUrl.openStream()) as JsonObject
        /* NSI uses (atm) a slightly different format than the normal presets: The presets are in
           a sub-object called "presets" */
        val presets = nsiPresetsJson.obj("presets")!!
        for (entry in presets.entries) {
            val id = entry.key
            val presetJson = entry.value as JsonObject
            val imageURL = presetJson["imageURL"] as String?
            if (imageURL != null) {
                val smallImageUrl = when {
                    imageURL.contains("graph.facebook.com") && imageURL.endsWith("large") -> {
                        // small is about 50x50px. large would be 200x200px
                        imageURL.replaceRange(imageURL.length - 5, imageURL.length, "small")
                    }
                    imageURL.contains("commons.wikimedia.org") && imageURL.endsWith("width=100") -> {
                        imageURL.replaceRange(imageURL.length - 3, imageURL.length, "50")
                    }
                    imageURL.contains("pbs.twimg.com/profile_images") -> {
                        // normal is 48x48px, bigger is about 128x128px
                        imageURL.replace("_bigger.", "_normal.")
                    }
                    else -> {
                        imageURL
                    }
                }
                try {
                    val conn = URL(smallImageUrl)
                    val suffix = when (conn.openConnection().contentType) {
                        "image/jpeg" -> "jpg"
                        "image/png" -> "png"
                        "image/webp" -> "webp"
                        else -> throw UnsupportedFormatException("only png, jpg, webp are supported")
                    }

                    val targetFile = File("$targetDir/$id.$suffix")
                    targetFile.parentFile.mkdirs()

                    conn.openStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: FileNotFoundException) {
                    println("$id: $imageURL not found")
                } catch (e: UnsupportedFormatException) {
                    println("$id: $imageURL unsupported format")
                }
            }
        }
    }
}

class UnsupportedFormatException(reason: String) : Exception(reason)
