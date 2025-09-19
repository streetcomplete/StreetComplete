package de.westnordost.streetcomplete.osm.mtb_scale

import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale.Value.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.empty_107
import de.westnordost.streetcomplete.resources.mtb_scale_0
import de.westnordost.streetcomplete.resources.mtb_scale_1
import de.westnordost.streetcomplete.resources.mtb_scale_2
import de.westnordost.streetcomplete.resources.mtb_scale_3
import de.westnordost.streetcomplete.resources.mtb_scale_4
import de.westnordost.streetcomplete.resources.mtb_scale_5
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_0
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_0_description
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_1
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_1_description
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_2
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_2_description
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_3
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_3_description
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_4
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_4_description
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_5
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_5_description
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_6
import de.westnordost.streetcomplete.resources.overlay_mtb_scale_6_description
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val MtbScale.Value.title: StringResource get() = when (this) {
    ZERO -> Res.string.overlay_mtb_scale_0
    ONE -> Res.string.overlay_mtb_scale_1
    TWO -> Res.string.overlay_mtb_scale_2
    THREE -> Res.string.overlay_mtb_scale_3
    FOUR -> Res.string.overlay_mtb_scale_4
    FIVE -> Res.string.overlay_mtb_scale_5
    SIX -> Res.string.overlay_mtb_scale_6
}

val MtbScale.Value.description: StringResource get() = when (this) {
    ZERO -> Res.string.overlay_mtb_scale_0_description
    ONE -> Res.string.overlay_mtb_scale_1_description
    TWO -> Res.string.overlay_mtb_scale_2_description
    THREE -> Res.string.overlay_mtb_scale_3_description
    FOUR -> Res.string.overlay_mtb_scale_4_description
    FIVE -> Res.string.overlay_mtb_scale_5_description
    SIX -> Res.string.overlay_mtb_scale_6_description
}

val MtbScale.Value.icon: DrawableResource get() = when (this) {
    ZERO -> Res.drawable.mtb_scale_0
    ONE -> Res.drawable.mtb_scale_1
    TWO -> Res.drawable.mtb_scale_2
    THREE -> Res.drawable.mtb_scale_3
    FOUR -> Res.drawable.mtb_scale_4
    FIVE -> Res.drawable.mtb_scale_5
    SIX -> Res.drawable.empty_107
}
