package de.westnordost.streetcomplete.osm.lit

import de.westnordost.streetcomplete.osm.lit.LitStatus.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val LitStatus.icon: DrawableResource get() = when (this) {
    YES -> Res.drawable.lit_yes
    NO -> Res.drawable.lit_no
    AUTOMATIC -> Res.drawable.lit_automatic
    NIGHT_AND_DAY -> Res.drawable.lit_24_7
    UNSUPPORTED -> Res.drawable.lit_unsupported
}

val LitStatus.title: StringResource get() = when (this) {
    YES -> Res.string.lit_value_yes
    NO -> Res.string.lit_value_no
    AUTOMATIC -> Res.string.lit_value_automatic
    NIGHT_AND_DAY -> Res.string.lit_value_24_7
    UNSUPPORTED -> Res.string.lit_value_unsupported
}
