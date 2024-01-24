package de.westnordost.streetcomplete.quests.smoothness

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.BAD
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.EXCELLENT
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.GOOD
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.HORRIBLE
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.IMPASSABLE
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.INTERMEDIATE
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.VERY_BAD
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.VERY_HORRIBLE
import de.westnordost.streetcomplete.util.ktx.asImageSpan
import de.westnordost.streetcomplete.view.CharSequenceText
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun Iterable<Smoothness>.toItems(context: Context, surface: String) =
    mapNotNull { it.asItem(context, surface) }

// return null if not a valid combination
fun Smoothness.asItem(context: Context, surface: String): DisplayItem<Smoothness>? {
    val imageResId = getImageResId(surface) ?: return null
    val descriptionResId = getDescriptionResId(surface) ?: return null

    val title = context.getString(titleResId)
    val stringBuilder = SpannableStringBuilder(title)
    stringBuilder.append(" X") // one space char + one placeholder char for the image span

    val iconDrawable = context.getDrawable(icon) ?: return null
    stringBuilder.setSpan(
        iconDrawable.asImageSpan(36, 36),
        title.length + 1,
        title.length + 2,
        Spannable.SPAN_INCLUSIVE_INCLUSIVE
    )

    return Item2(
        this,
        ResImage(imageResId),
        CharSequenceText(stringBuilder),
        ResText(descriptionResId)
    )
}

/** return fitting vehicle type emoji that corresponds to the "usable by" column in the wiki */
val Smoothness.icon get(): @DrawableRes Int = when (this) {
    EXCELLENT ->     R.drawable.ic_smoothness_skateboard
    GOOD ->          R.drawable.ic_smoothness_scooter
    INTERMEDIATE ->  R.drawable.ic_smoothness_city_bike
    BAD ->           R.drawable.ic_smoothness_car
    VERY_BAD ->      R.drawable.ic_smoothness_suv
    HORRIBLE ->      R.drawable.ic_smoothness_pickup_truck
    VERY_HORRIBLE -> R.drawable.ic_smoothness_tractor
    IMPASSABLE ->    R.drawable.ic_smoothness_pedestrian
}

val Smoothness.titleResId get() = when (this) {
    EXCELLENT -> R.string.quest_smoothness_title_excellent
    GOOD -> R.string.quest_smoothness_title_good
    INTERMEDIATE -> R.string.quest_smoothness_title_intermediate
    BAD -> R.string.quest_smoothness_title_bad
    VERY_BAD -> R.string.quest_smoothness_title_very_bad
    HORRIBLE -> R.string.quest_smoothness_title_horrible
    VERY_HORRIBLE -> R.string.quest_smoothness_title_very_horrible
    IMPASSABLE -> R.string.quest_smoothness_title_impassable
}

fun Smoothness.getDescriptionResId(surface: String): Int? = when (surface) {
    "asphalt", "concrete", "concrete:plates" -> pavedDescriptionResId
    "sett" -> settDescriptionResId
    "paving_stones" -> pavingStonesDescriptionResId
    "compacted", "gravel", "fine_gravel" -> compactedOrGravelDescriptionResId
    else -> null
} ?: descriptionResIdFallback

private val Smoothness.descriptionResIdFallback: Int? get() = when (this) {
    HORRIBLE -> R.string.quest_smoothness_description_horrible
    VERY_HORRIBLE -> R.string.quest_smoothness_description_very_horrible
    IMPASSABLE -> R.string.quest_smoothness_description_impassable
    else -> null
}

fun Smoothness.getImageResId(surface: String): Int? = when (surface) {
    "asphalt" -> asphaltImageResId
    "concrete", "concrete:plates" -> concreteImageResId
    "sett" -> settImageResId
    "paving_stones" -> pavingStonesImageResId
    "compacted" -> compactedImageResId
    "gravel", "fine_gravel" -> gravelImageResId
    else -> null
}

private val Smoothness.asphaltImageResId get() = when (this) {
    EXCELLENT -> R.drawable.surface_asphalt_excellent
    GOOD -> R.drawable.surface_asphalt_good
    INTERMEDIATE -> R.drawable.surface_asphalt_intermediate
    BAD -> R.drawable.surface_asphalt_bad
    VERY_BAD -> R.drawable.surface_asphalt_very_bad
    else -> null
}

private val Smoothness.concreteImageResId get() = when (this) {
    EXCELLENT -> R.drawable.surface_concrete_excellent
    GOOD -> R.drawable.surface_concrete_good
    INTERMEDIATE -> R.drawable.surface_concrete_intermediate
    BAD -> R.drawable.surface_concrete_bad
    VERY_BAD -> R.drawable.surface_concrete_very_bad
    else -> null
}

private val Smoothness.pavedDescriptionResId get() = when (this) {
    EXCELLENT -> R.string.quest_smoothness_description_excellent_paved
    GOOD -> R.string.quest_smoothness_description_good_paved
    INTERMEDIATE -> R.string.quest_smoothness_description_intermediate_paved
    BAD -> R.string.quest_smoothness_description_bad_paved
    VERY_BAD -> R.string.quest_smoothness_description_very_bad_paved
    else -> null
}

private val Smoothness.settImageResId get() = when (this) {
    GOOD -> R.drawable.surface_sett_good
    INTERMEDIATE -> R.drawable.surface_sett_intermediate
    BAD -> R.drawable.surface_sett_bad
    VERY_BAD -> R.drawable.surface_sett_very_bad
    else -> null
}

private val Smoothness.settDescriptionResId get() = when (this) {
    GOOD -> R.string.quest_smoothness_description_good_sett
    INTERMEDIATE -> R.string.quest_smoothness_description_intermediate_sett
    BAD -> R.string.quest_smoothness_description_bad_sett
    VERY_BAD -> R.string.quest_smoothness_description_very_bad_sett
    else -> null
}

private val Smoothness.pavingStonesImageResId get() = when (this) {
    EXCELLENT -> R.drawable.surface_paving_stones_excellent
    GOOD -> R.drawable.surface_paving_stones_good
    INTERMEDIATE -> R.drawable.surface_paving_stones_intermediate
    BAD -> R.drawable.surface_paving_stones_bad
    VERY_BAD -> R.drawable.surface_paving_stones_very_bad
    else -> null
}

private val Smoothness.pavingStonesDescriptionResId get() = when (this) {
    EXCELLENT -> R.string.quest_smoothness_description_excellent_paving_stones
    GOOD -> R.string.quest_smoothness_description_good_paving_stones
    INTERMEDIATE -> R.string.quest_smoothness_description_intermediate_paving_stones
    BAD -> R.string.quest_smoothness_description_bad_paving_stones
    VERY_BAD -> R.string.quest_smoothness_description_very_bad_paving_stones
    else -> null
}

private val Smoothness.compactedImageResId get() = when (this) {
    GOOD -> R.drawable.surface_compacted_good
    INTERMEDIATE -> R.drawable.surface_compacted_intermediate
    BAD -> R.drawable.surface_compacted_bad
    VERY_BAD -> R.drawable.surface_compacted_very_bad
    HORRIBLE -> R.drawable.surface_unpaved_horrible
    VERY_HORRIBLE -> R.drawable.surface_unpaved_very_horrible
    IMPASSABLE -> R.drawable.surface_unpaved_impassable
    else -> null
}

private val Smoothness.gravelImageResId get() = when (this) {
    INTERMEDIATE -> R.drawable.surface_gravel_intermediate
    BAD -> R.drawable.surface_gravel_bad
    VERY_BAD -> R.drawable.surface_gravel_very_bad
    HORRIBLE -> R.drawable.surface_unpaved_horrible
    VERY_HORRIBLE -> R.drawable.surface_unpaved_very_horrible
    IMPASSABLE -> R.drawable.surface_unpaved_impassable
    else -> null
}

private val Smoothness.compactedOrGravelDescriptionResId get() = when (this) {
    GOOD -> R.string.quest_smoothness_description_good_compacted_gravel
    INTERMEDIATE -> R.string.quest_smoothness_description_intermediate_compacted_gravel
    BAD -> R.string.quest_smoothness_description_bad_compacted_gravel
    VERY_BAD -> R.string.quest_smoothness_description_very_bad_compacted_gravel
    else -> null
}
