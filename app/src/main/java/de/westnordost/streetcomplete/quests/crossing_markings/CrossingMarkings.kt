package de.westnordost.streetcomplete.quests.crossing_markings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class CrossingMarkings(
    val osmValue: String,
    @DrawableRes val imageResId: Int?,
    @StringRes val titleResId: Int?,
) {
    YES(
        osmValue = "yes",
        imageResId = null,
        titleResId = null
    ),
    NO(
        osmValue = "no",
        imageResId = R.drawable.crossing_markings_no,
        titleResId = R.string.quest_crossing_marking_value_no
    ),
    ZEBRA(
        osmValue = "zebra",
        imageResId = R.drawable.crossing_markings_zebra,
        titleResId = R.string.quest_crossing_marking_value_zebra
    ),
    LINES(
        osmValue = "lines",
        imageResId = R.drawable.crossing_markings_lines,
        titleResId = R.string.quest_crossing_marking_value_lines
    ),
    LADDER(
        osmValue = "ladder",
        imageResId = R.drawable.crossing_markings_ladder,
        titleResId = R.string.quest_crossing_marking_value_ladder
    ),
    DASHES(
        osmValue = "dashes",
        imageResId = R.drawable.crossing_markings_dashes,
        titleResId = R.string.quest_crossing_marking_value_dashes
    ),
    DOTS(
        osmValue = "dots",
        imageResId = R.drawable.crossing_markings_dots,
        titleResId = R.string.quest_crossing_marking_value_dots
    ),
    SURFACE(
        osmValue = "surface",
        imageResId = R.drawable.crossing_markings_surface,
        titleResId = R.string.quest_crossing_marking_value_surface
    ),
    LADDER_SKEWED(
        osmValue = "ladder:skewed",
        imageResId = R.drawable.crossing_markings_ladder_skewed,
        titleResId = R.string.quest_crossing_marking_value_ladder_skewed
    ),
    ZEBRA_PAIRED(
        osmValue = "zebra:paired",
        imageResId = R.drawable.crossing_markings_zebra_paired,
        titleResId = R.string.quest_crossing_marking_value_zebra_paired
    ),
    ZEBRA_BICOLOUR(
        osmValue = "zebra:bicolour",
        imageResId = R.drawable.crossing_markings_zebra_bicolour,
        titleResId = R.string.quest_crossing_marking_value_zebra_bicolour
    ),
    ZEBRA_DOUBLE(
        osmValue = "zebra:double",
        imageResId = R.drawable.crossing_markings_zebra_double,
        titleResId = R.string.quest_crossing_marking_value_zebra_double
    ),
    LADDER_PAIRED(
        osmValue = "ladder:paired",
        imageResId = R.drawable.crossing_markings_ladder_paired,
        titleResId = R.string.quest_crossing_marking_value_ladder_paired
    ),
    ZEBRA_DOTS(
        osmValue = "zebra;dots",
        imageResId = R.drawable.crossing_markings_zebra_dots,
        titleResId = R.string.quest_crossing_marking_value_zebra_dots
    ),
    PICTOGRAMS(
        osmValue = "pictograms",
        imageResId = R.drawable.crossing_markings_pictograms,
        titleResId = R.string.quest_crossing_marking_value_pictograms
    )
}

fun Collection<CrossingMarkings>.toItems() = map { it.asItem() }

fun CrossingMarkings.asItem(): DisplayItem<CrossingMarkings> = Item(this, imageResId, titleResId)
