package de.westnordost.streetcomplete.ui.ktx

import com.charleskorn.kaml.Yaml
import de.westnordost.streetcomplete.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import org.jetbrains.compose.resources.MissingResourceException

expect fun Res.exists(path: String): Boolean

suspend inline fun <reified T> Res.readYaml(path: String, yaml: Yaml = Yaml.default): T {
    val yml = readBytes(path).decodeToString()
    return withContext(Dispatchers.Default) { yaml.decodeFromString(yml) }
}

suspend inline fun <reified T> Res.readYamlOrNull(path: String, yaml: Yaml = Yaml.default): T? =
    try { readYaml(path, yaml) } catch (_: MissingResourceException) { null }

suspend fun Res.readBytesOrNull(path: String): ByteArray? =
    try { readBytes(path) } catch (_: MissingResourceException) { null }
