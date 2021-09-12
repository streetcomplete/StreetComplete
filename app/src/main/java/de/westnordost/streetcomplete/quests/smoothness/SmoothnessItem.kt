package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.smoothness.Smoothness.*
import de.westnordost.streetcomplete.view.image_select.Item

fun Array<Smoothness>.toItems(surface: String, highway: String) = this.mapNotNull { it.asItem(surface, highway) }

// return null if not a valid combination
fun Smoothness.asItem(surface: String, highway: String): Item<Smoothness>? {
    val imageResId = getImageResId(surface) ?: return null
    val descriptionResId = getDescriptionResId(surface, highway) ?: return null
    return Item(this, imageResId, getTitleRedId(), descriptionResId)
}

fun Smoothness.getTitleRedId() = when (this) {
    EXCELLENT -> R.string.quest_smoothness_title_excellent
    GOOD -> R.string.quest_smoothness_title_good
    INTERMEDIATE -> R.string.quest_smoothness_title_intermediate
    BAD -> R.string.quest_smoothness_title_bad
    VERY_BAD -> R.string.quest_smoothness_title_very_bad
    HORRIBLE -> R.string.quest_smoothness_title_horrible
    VERY_HORRIBLE -> R.string.quest_smoothness_title_very_horrible
    IMPASSABLE -> R.string.quest_smoothness_title_impassable
}

// TODO: else should always throw exception!
fun Smoothness.getDescriptionResId(surface: String, highway: String) = when (this) {
    EXCELLENT -> when (surface) {
        // no "excellent" for roads with paving stones
        "paving_stones" -> if (highway in ALL_PATHS_EXCEPT_STEPS)
                R.string.quest_smoothness_description_excellent_paving_stones
            else
                null
        else -> R.string.quest_smoothness_description_excellent
    }
    GOOD -> when (surface) {
        "paving_stones" -> R.string.quest_smoothness_description_good_paving_stones
        else -> R.string.quest_smoothness_description_good
    }
    INTERMEDIATE -> when (surface) {
        "paving_stones", "sett" -> R.string.quest_smoothness_description_intermediate_paving_stones
        "compacted" -> R.string.quest_smoothness_description_intermediate_compacted
        else -> when (highway) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_intermediate_path
            else -> R.string.quest_smoothness_description_intermediate
        }
    }
    BAD -> when (surface) {
        "sett" -> R.string.quest_smoothness_description_bad_sett
        "paving_stones" -> R.string.quest_smoothness_description_bad_paving_stones
        else -> when (highway) {
            in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_bad_path
            else -> R.string.quest_smoothness_description_bad_road
        }
    }
    VERY_BAD -> when (highway) {
        in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_very_bad_path
        else -> R.string.quest_smoothness_description_very_bad_road
    }
    HORRIBLE -> when (highway) {
        in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_horrible
        else -> R.string.quest_smoothness_description_horrible
    }
    VERY_HORRIBLE -> when (highway) {
        in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_very_horrible
        else -> R.string.quest_smoothness_description_very_horrible
    }
    IMPASSABLE -> when (highway) {
        in ALL_PATHS_EXCEPT_STEPS -> R.string.quest_smoothness_description_impassable
        else -> R.string.quest_smoothness_description_impassable
    }
}

// TODO: extend
fun Smoothness.getImageResId(surface: String): Int? = when(surface) {
    "asphalt" -> getAsphaltImageResId()
    "sett" -> getSettImageResId()
    "paving_stones" -> getPavingStonesImageResId()
    "compacted" -> getCompactedImageResId()
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
    VERY_BAD -> R.drawable.surface_paving_stones_very_bad
    else -> null
}

fun Smoothness.getCompactedImageResId() = when (this) {
    INTERMEDIATE -> R.drawable.surface_compacted_intermediate
    BAD -> R.drawable.surface_compacted_bad
    VERY_BAD -> R.drawable.surface_compacted_very_bad
    HORRIBLE -> R.drawable.surface_compacted_horrible
    else -> null
}
