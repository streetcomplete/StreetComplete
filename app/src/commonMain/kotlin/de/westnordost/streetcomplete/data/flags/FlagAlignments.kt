package de.westnordost.streetcomplete.data.flags

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readYaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias FlagAlignments = Map<String, FlagAlignment>

@Serializable
enum class FlagAlignment {
    @SerialName("left") Left,
    @SerialName("center-left") CenterLeft,
    @SerialName("center") Center,
    @SerialName("center-right") CenterRight,
    @SerialName("right") Right,
    @SerialName("stretch") Stretch
}

suspend fun Res.readFlagAlignments(): FlagAlignments =
    readYaml("files/flag_alignments.yml")
