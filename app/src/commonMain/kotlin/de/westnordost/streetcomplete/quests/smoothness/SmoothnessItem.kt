package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.quests.smoothness.Smoothness.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** return fitting vehicle type emoji that corresponds to the "usable by" column in the wiki */
val Smoothness.icon: DrawableResource get() = when (this) {
    EXCELLENT ->     Res.drawable.smoothness_skateboard
    GOOD ->          Res.drawable.smoothness_scooter
    INTERMEDIATE ->  Res.drawable.smoothness_city_bike
    BAD ->           Res.drawable.smoothness_car
    VERY_BAD ->      Res.drawable.smoothness_suv
    HORRIBLE ->      Res.drawable.smoothness_pickup_truck
    VERY_HORRIBLE -> Res.drawable.smoothness_tractor
    IMPASSABLE ->    Res.drawable.smoothness_pedestrian
}

val Smoothness.emoji: String get() = when (this) {
    EXCELLENT ->     "🛹"
    GOOD ->          "🛴"
    INTERMEDIATE ->  "🚲"
    BAD ->           "🚗"
    VERY_BAD ->      "🚙"
    HORRIBLE ->      "🛻"
    VERY_HORRIBLE -> "🚜"
    IMPASSABLE ->    "🚶"
}

val Smoothness.title: StringResource get() = when (this) {
    EXCELLENT ->     Res.string.quest_smoothness_title_excellent
    GOOD ->          Res.string.quest_smoothness_title_good
    INTERMEDIATE ->  Res.string.quest_smoothness_title_intermediate
    BAD ->           Res.string.quest_smoothness_title_bad
    VERY_BAD ->      Res.string.quest_smoothness_title_very_bad
    HORRIBLE ->      Res.string.quest_smoothness_title_horrible
    VERY_HORRIBLE -> Res.string.quest_smoothness_title_very_horrible
    IMPASSABLE ->    Res.string.quest_smoothness_title_impassable
}

fun Smoothness.getDescription(surface: String?): StringResource? = when (surface) {
    "asphalt", "concrete", "concrete:plates" -> pavedDescription
    "sett" -> settDescription
    "paving_stones" -> pavingStonesDescription
    "compacted", "gravel", "fine_gravel" -> compactedOrGravelDescription
    else -> null
} ?: descriptionFallback

private val Smoothness.descriptionFallback: StringResource? get() = when (this) {
    HORRIBLE -> Res.string.quest_smoothness_description_horrible
    VERY_HORRIBLE -> Res.string.quest_smoothness_description_very_horrible
    IMPASSABLE -> Res.string.quest_smoothness_description_impassable
    else -> null
}

fun Smoothness.getImage(surface: String?): DrawableResource? = when (surface) {
    "asphalt" -> asphaltImage
    "concrete", "concrete:plates" -> concreteImage
    "sett" -> settImage
    "paving_stones" -> pavingStonesImage
    "compacted" -> compactedImage
    "gravel", "fine_gravel" -> gravelImage
    else -> null
}

private val Smoothness.asphaltImage: DrawableResource? get() = when (this) {
    EXCELLENT -> Res.drawable.surface_asphalt_excellent
    GOOD -> Res.drawable.surface_asphalt_good
    INTERMEDIATE -> Res.drawable.surface_asphalt_intermediate
    BAD -> Res.drawable.surface_asphalt_bad
    VERY_BAD -> Res.drawable.surface_asphalt_very_bad
    else -> null
}

private val Smoothness.concreteImage: DrawableResource? get() = when (this) {
    EXCELLENT -> Res.drawable.surface_concrete_excellent
    GOOD -> Res.drawable.surface_concrete_good
    INTERMEDIATE -> Res.drawable.surface_concrete_intermediate
    BAD -> Res.drawable.surface_concrete_bad
    VERY_BAD -> Res.drawable.surface_concrete_very_bad
    else -> null
}

private val Smoothness.settImage: DrawableResource? get() = when (this) {
    GOOD -> Res.drawable.surface_sett_good
    INTERMEDIATE -> Res.drawable.surface_sett_intermediate
    BAD -> Res.drawable.surface_sett_bad
    VERY_BAD -> Res.drawable.surface_sett_very_bad
    else -> null
}

private val Smoothness.pavingStonesImage get() = when (this) {
    EXCELLENT -> Res.drawable.surface_paving_stones_excellent
    GOOD -> Res.drawable.surface_paving_stones_good
    INTERMEDIATE -> Res.drawable.surface_paving_stones_intermediate
    BAD -> Res.drawable.surface_paving_stones_bad
    VERY_BAD -> Res.drawable.surface_paving_stones_very_bad
    else -> null
}

private val Smoothness.compactedImage get() = when (this) {
    GOOD -> Res.drawable.surface_compacted_good
    INTERMEDIATE -> Res.drawable.surface_compacted_intermediate
    BAD -> Res.drawable.surface_compacted_bad
    VERY_BAD -> Res.drawable.surface_compacted_very_bad
    HORRIBLE -> Res.drawable.surface_unpaved_horrible
    VERY_HORRIBLE -> Res.drawable.surface_unpaved_very_horrible
    IMPASSABLE -> Res.drawable.surface_unpaved_impassable
    else -> null
}

private val Smoothness.gravelImage: DrawableResource? get() = when (this) {
    INTERMEDIATE -> Res.drawable.surface_gravel_intermediate
    BAD -> Res.drawable.surface_gravel_bad
    VERY_BAD -> Res.drawable.surface_gravel_very_bad
    HORRIBLE -> Res.drawable.surface_unpaved_horrible
    VERY_HORRIBLE -> Res.drawable.surface_unpaved_very_horrible
    IMPASSABLE -> Res.drawable.surface_unpaved_impassable
    else -> null
}

private val Smoothness.pavedDescription: StringResource? get() = when (this) {
    EXCELLENT -> Res.string.quest_smoothness_description_excellent_paved
    GOOD -> Res.string.quest_smoothness_description_good_paved
    INTERMEDIATE -> Res.string.quest_smoothness_description_intermediate_paved
    BAD -> Res.string.quest_smoothness_description_bad_paved
    VERY_BAD -> Res.string.quest_smoothness_description_very_bad_paved
    else -> null
}

private val Smoothness.settDescription: StringResource? get() = when (this) {
    GOOD -> Res.string.quest_smoothness_description_good_sett
    INTERMEDIATE -> Res.string.quest_smoothness_description_intermediate_sett
    BAD -> Res.string.quest_smoothness_description_bad_sett
    VERY_BAD -> Res.string.quest_smoothness_description_very_bad_sett
    else -> null
}

private val Smoothness.pavingStonesDescription: StringResource? get() = when (this) {
    EXCELLENT -> Res.string.quest_smoothness_description_excellent_paving_stones
    GOOD -> Res.string.quest_smoothness_description_good_paving_stones
    INTERMEDIATE -> Res.string.quest_smoothness_description_intermediate_paving_stones
    BAD -> Res.string.quest_smoothness_description_bad_paving_stones
    VERY_BAD -> Res.string.quest_smoothness_description_very_bad_paving_stones
    else -> null
}

private val Smoothness.compactedOrGravelDescription: StringResource? get() = when (this) {
    GOOD -> Res.string.quest_smoothness_description_good_compacted_gravel
    INTERMEDIATE -> Res.string.quest_smoothness_description_intermediate_compacted_gravel
    BAD -> Res.string.quest_smoothness_description_bad_compacted_gravel
    VERY_BAD -> Res.string.quest_smoothness_description_very_bad_compacted_gravel
    else -> null
}
