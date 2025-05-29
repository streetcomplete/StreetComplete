package de.westnordost.streetcomplete.data.res

import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.layout.ContentScale
import com.charleskorn.kaml.Yaml
import de.westnordost.stretcomplete.resources.Res
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

typealias FlagAlignments = Map<String, FlagAlignment>

suspend fun Res.readFlagAlignments(): FlagAlignments {
    val yml = readBytes("files/flag_alignments.yml").decodeToString()
    return Yaml.default.decodeFromString<FlagAlignments>(yml)
}

@Serializable
enum class FlagAlignment {
    @SerialName("left") Left,
    @SerialName("center-left") CenterLeft,
    @SerialName("center") Center,
    @SerialName("center-right") CenterRight,
    @SerialName("right") Right,
    @SerialName("stretch") Stretch;

    val alignment: Alignment get() = when (this) {
        Left ->        AbsoluteAlignment.CenterLeft
        CenterLeft ->  BiasAbsoluteAlignment(-0.5f, 0f)
        Center ->      Alignment.Center
        CenterRight -> BiasAbsoluteAlignment(+0.5f, 0f)
        Right ->       AbsoluteAlignment.CenterRight
        Stretch ->     Alignment.Center
    }

    val contentScale: ContentScale get() = when (this) {
        Stretch -> ContentScale.FillBounds
        else -> ContentScale.Crop
    }
}

