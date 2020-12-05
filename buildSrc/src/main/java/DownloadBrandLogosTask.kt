
import com.beust.klaxon.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import com.beust.klaxon.Parser
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL

open class DownloadBrandLogosTask : DefaultTask() {
    @get:Input var presetsPath: String? = null
    @get:Input var targetDir: String? = null

    @TaskAction fun run() {
        val presetsPath = presetsPath ?: return
        val targetDir = targetDir ?: return

        val presetsJson = Parser.default().parse(presetsPath) as JsonObject
        for (entry in presetsJson.entries) {
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
                    val suffix = when(conn.openConnection().contentType) {
                        "image/jpeg" -> "jpg"
                        "image/png" -> "png"
                        "image/webp" -> "webp"
                        else -> throw UnsupportedFormatException("only png, jpg, webp are supported")
                    }

                    val targetFile = File("$targetDir/$id.$suffix")
                    targetFile.parentFile.mkdirs()

                    URL(smallImageUrl).openStream().use { input ->
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
