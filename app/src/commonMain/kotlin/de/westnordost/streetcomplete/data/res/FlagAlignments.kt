package de.westnordost.streetcomplete.data.res

import com.charleskorn.kaml.Yaml
import de.westnordost.stretcomplete.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

typealias FlagAlignments = Map<String, FlagAlignment>

suspend fun Res.readFlagAlignments(): FlagAlignments {
    val yml = readBytes("files/flag_alignments.yml").decodeToString()
    return withContext(Dispatchers.Default) { Yaml.default.decodeFromString<FlagAlignments>(yml) }
}

@Serializable
enum class FlagAlignment {
    @SerialName("left") Left,
    @SerialName("center-left") CenterLeft,
    @SerialName("center") Center,
    @SerialName("center-right") CenterRight,
    @SerialName("right") Right,
    @SerialName("stretch") Stretch;
}
