package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.*
import de.westnordost.streetcomplete.view.image_select.Item

fun Array<Smoothness>.toItems(surface: String, highway: String) = this.mapNotNull { it.asItem(surface, highway) }

// return null if not a valid combination
fun Smoothness.asItem(surface: String, highway: String): Item<Smoothness>? {
    val imageResId = getImageResId(surface) ?: return null
    val descriptionResId = getDescriptionResId(surface, highway) ?: return null
    return Item(this, imageResId, getTitleResId(), descriptionResId)
}

fun Smoothness.getTitleResId() = when (this) {
    EXCELLENT -> R.string.quest_smoothness_title_excellent
    GOOD -> R.string.quest_smoothness_title_good
    INTERMEDIATE -> R.string.quest_smoothness_title_intermediate
    BAD -> R.string.quest_smoothness_title_bad
    VERY_BAD -> R.string.quest_smoothness_title_very_bad
    HORRIBLE -> R.string.quest_smoothness_title_horrible
    VERY_HORRIBLE -> R.string.quest_smoothness_title_very_horrible
    IMPASSABLE -> R.string.quest_smoothness_title_impassable
}

fun Smoothness.getDescriptionResId(surface: String, highway: String) = when (this) {
    EXCELLENT -> when (surface) {
        // no "excellent" for roads with paving stones
        "paving_stones" -> if (highway in ALL_PATHS_EXCEPT_STEPS)
                R.string.quest_smoothness_description_excellent_paving_stones
            else
                null
        "asphalt" -> R.string.quest_smoothness_description_excellent
        else -> null
    }
    GOOD -> when (surface) {
        "paving_stones" -> R.string.quest_smoothness_description_good_paving_stones
        "asphalt" -> R.string.quest_smoothness_description_good
        else -> null
    }
    INTERMEDIATE -> when (surface) {
        "paving_stones" -> R.string.quest_smoothness_description_intermediate_paving_stones
        "sett" -> R.string.quest_smoothness_description_intermediate_sett
        "compacted" -> R.string.quest_smoothness_description_intermediate_compacted
        "gravel" -> R.string.quest_smoothness_description_intermediate_gravel
        "asphalt" -> when (highway) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_intermediate_path
            else -> R.string.quest_smoothness_description_intermediate_road
        }
        else -> null
    }
    BAD -> when (surface) {
        "sett" -> R.string.quest_smoothness_description_bad_sett
        "paving_stones" -> R.string.quest_smoothness_description_bad_paving_stones
        "asphalt", "compacted", "gravel" -> when (highway) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_bad_path
            else -> R.string.quest_smoothness_description_bad_road
        }
        else -> null
    }
    VERY_BAD -> when (surface) {
        "sett" -> R.string.quest_smoothness_description_very_bad_sett
        else -> when (highway) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_very_bad_path
            else -> R.string.quest_smoothness_description_very_bad_road
        }
    }
    // split up?
    HORRIBLE -> R.string.quest_smoothness_description_horrible
    VERY_HORRIBLE -> R.string.quest_smoothness_description_very_horrible
    IMPASSABLE -> R.string.quest_smoothness_description_impassable
}

// should contain all surfaces in AddRoadSmoothness.SURFACES_FOR_SMOOTHNESS
fun Smoothness.getImageResId(surface: String): Int? = when(surface) {
    "asphalt" -> getAsphaltImageResId()
    "sett" -> getSettImageResId()
    "paving_stones" -> getPavingStonesImageResId()
    "compacted" -> getCompactedImageResId()
    "gravel" -> getGravelImageResId()
    else -> throw IllegalStateException()
}

fun Smoothness.getAsphaltImageResId() = when (this) {
    EXCELLENT -> R.drawable.surface_asphalt_excellent
    GOOD -> R.drawable.surface_asphalt_good
    INTERMEDIATE -> R.drawable.surface_asphalt_intermediate
    BAD -> R.drawable.surface_asphalt_bad
    VERY_BAD -> R.drawable.surface_asphalt_very_bad
    else -> null
}

fun Smoothness.getSettImageResId() = when (this) {
    INTERMEDIATE -> R.drawable.surface_sett_intermediate
    BAD -> R.drawable.surface_sett_bad
    VERY_BAD -> R.drawable.surface_sett_very_bad
    else -> null
}

fun Smoothness.getPavingStonesImageResId() = when (this) {
    EXCELLENT -> R.drawable.surface_paving_stones_excellent
    GOOD -> R.drawable.surface_paving_stones_good
    INTERMEDIATE -> R.drawable.surface_paving_stones_intermediate
    BAD -> R.drawable.surface_paving_stones_bad
    else -> null
}

fun Smoothness.getCompactedImageResId() = when (this) {
    INTERMEDIATE -> R.drawable.surface_compacted_intermediate
    BAD -> R.drawable.surface_compacted_bad
    VERY_BAD -> R.drawable.surface_compacted_very_bad
    HORRIBLE -> R.drawable.surface_unpaved_horrible
    VERY_HORRIBLE -> R.drawable.surface_unpaved_very_horrible
    IMPASSABLE -> R.drawable.surface_unpaved_impassable
    else -> null
}

fun Smoothness.getGravelImageResId() = when (this) {
    INTERMEDIATE -> R.drawable.surface_gravel_intermediate
    BAD -> R.drawable.surface_gravel_bad
    VERY_BAD -> R.drawable.surface_gravel_very_bad
    HORRIBLE -> R.drawable.surface_gravel_horrible
    VERY_HORRIBLE -> R.drawable.surface_unpaved_very_horrible
    IMPASSABLE -> R.drawable.surface_unpaved_impassable
    else -> null
}
