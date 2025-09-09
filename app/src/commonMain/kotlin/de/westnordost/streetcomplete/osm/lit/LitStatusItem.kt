package de.westnordost.streetcomplete.osm.lit

import de.westnordost.streetcomplete.osm.lit.LitStatus.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.lit_24_7
import de.westnordost.streetcomplete.resources.lit_automatic
import de.westnordost.streetcomplete.resources.lit_no
import de.westnordost.streetcomplete.resources.lit_unsupported
import de.westnordost.streetcomplete.resources.lit_value_24_7
import de.westnordost.streetcomplete.resources.lit_value_automatic
import de.westnordost.streetcomplete.resources.lit_value_no
import de.westnordost.streetcomplete.resources.lit_value_unsupported
import de.westnordost.streetcomplete.resources.lit_value_yes
import de.westnordost.streetcomplete.resources.lit_yes
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
