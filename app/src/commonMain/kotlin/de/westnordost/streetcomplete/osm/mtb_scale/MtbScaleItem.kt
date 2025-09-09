package de.westnordost.streetcomplete.osm.mtb_scale

import de.westnordost.streetcomplete.resources.Res
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

val MtbScale.title: StringResource? get() = when (value) {
    0 -> Res.string.overlay_mtb_scale_0
    1 -> Res.string.overlay_mtb_scale_1
    2 -> Res.string.overlay_mtb_scale_2
    3 -> Res.string.overlay_mtb_scale_3
    4 -> Res.string.overlay_mtb_scale_4
    5 -> Res.string.overlay_mtb_scale_5
    6 -> Res.string.overlay_mtb_scale_6
    else -> null
}

val MtbScale.description: StringResource? get() = when (value) {
    0 -> Res.string.overlay_mtb_scale_0_description
    1 -> Res.string.overlay_mtb_scale_1_description
    2 -> Res.string.overlay_mtb_scale_2_description
    3 -> Res.string.overlay_mtb_scale_3_description
    4 -> Res.string.overlay_mtb_scale_4_description
    5 -> Res.string.overlay_mtb_scale_5_description
    6 -> Res.string.overlay_mtb_scale_6_description
    else -> null
}

private val MtbScale.icon: DrawableResource? get() = when (value) {
    0 -> Res.drawable.mtb_scale_0
    1 -> Res.drawable.mtb_scale_1
    2 -> Res.drawable.mtb_scale_2
    3 -> Res.drawable.mtb_scale_3
    4 -> Res.drawable.mtb_scale_4
    5 -> Res.drawable.mtb_scale_5
    6 -> null
    else -> null
}
