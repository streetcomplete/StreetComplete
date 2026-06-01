import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CopyIconsTask : DefaultTask() {
    @get:Input lateinit var sourceDir: String
    @get:Input lateinit var targetDir: String
    @get:Input lateinit var filter: (String) -> Boolean
    @get:Input lateinit var indexFile: String

    @TaskAction
    fun run() {
        var fileNames = ArrayList<String>()
        for (file in File(sourceDir).listFiles().orEmpty()) {
            if (file.isFile) {
                if (filter(file.name)) {
                    file.copyTo(File(targetDir, file.name), overwrite = true)
                    fileNames.add(file.name.substringBeforeLast('.'))
                }
            }
        }
        fileNames.sort()

        val indexTargetFile = File(indexFile)
        indexTargetFile.parentFile.mkdirs()
        indexTargetFile.writeText("""
            package de.westnordost.streetcomplete.view

            import de.westnordost.streetcomplete.R
            import de.westnordost.streetcomplete.resources.*
            import org.jetbrains.compose.resources.DrawableResource

            fun DrawableResource.toAndroidResourceId(): Int? = icons[this]

            private val icons = mapOf(
                ${fileNames.joinToString(separator = ",\n                ") { fileName ->
            "Res.drawable.$fileName to R.drawable.$fileName"
        }}
            )
        """.trimIndent())
    }
}
