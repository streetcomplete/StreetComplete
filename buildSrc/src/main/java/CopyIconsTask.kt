import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CopyIconsTask : DefaultTask() {
    @get:InputDirectory lateinit var sourceDir: File
    @get:OutputDirectory lateinit var targetDir: File
    @get:Input lateinit var filter: (String) -> Boolean
    @get:OutputFile lateinit var indexFile: File

    @TaskAction
    fun run() {
        val fileNames = ArrayList<String>()
        val dimensions = listOf("", "-mdpi", "-hdpi", "-xhdpi", "-xxhdpi")
        val sourceDirs = dimensions.map { File(sourceDir.path + it) }
        val targetDirs = dimensions.map { File(targetDir.path + it) }

        for (i in dimensions.indices) {
            val srcDir = sourceDirs[i]
            val trgDir = targetDirs[i]
            for (file in srcDir.listFiles().orEmpty()) {
                if (file.isFile) {
                    if (filter(file.name)) {
                        file.copyTo(trgDir.resolve( file.name), overwrite = true)
                        fileNames.add(file.name.substringBeforeLast('.'))
                    }
                }
            }
        }
        fileNames.sort()

        indexFile.parentFile.mkdirs()
        indexFile.writeText("""
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
