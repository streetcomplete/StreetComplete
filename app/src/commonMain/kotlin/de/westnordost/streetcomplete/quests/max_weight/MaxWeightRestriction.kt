package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit.*
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.ktx.toShortString
import kotlinx.serialization.Serializable

@Serializable
sealed interface MaxWeightRestriction {
    val weight: Weight?

    fun isComplete(): Boolean = weight != null

    companion object {
        fun getSelectableEntries(countryCode: String): List<MaxWeightRestriction> = buildList {
            // no weight sign in France (see maxweightrating signs) - #6686
            if (countryCode != "FR") {
                add(MaxWeight())
            }
            if (countryCode == "SE") {
                add(MaxWeightCategorySweden())
            }
            // only known in select countries
            if (countryCode in listOf("AU", "CA", "US", "DE", "GB", "FR")) {
                add(MaxWeightRating())
            }
            // not known in GB
            if (countryCode != "GB") {
                add(MaxWeightRatingHgv())
            }
            add(MaxAxleLoad())
            add(MaxTandemAxleLoad())
        }
    }
}

@Serializable
data class MaxWeight(override val weight: Weight? = null) : MaxWeightRestriction
@Serializable
data class MaxWeightRating(override val weight: Weight? = null) : MaxWeightRestriction
@Serializable
data class MaxWeightRatingHgv(override val weight: Weight? = null) : MaxWeightRestriction
@Serializable
data class MaxAxleLoad(override val weight: Weight? = null) : MaxWeightRestriction
@Serializable
data class MaxTandemAxleLoad(override val weight: Weight? = null) : MaxWeightRestriction

@Serializable
data class MaxWeightCategorySweden(val category: Category? = null) : MaxWeightRestriction {
    override val weight: Weight? get() = category?.weight

    enum class Category(val value: Int, val weight: Weight) {
        One(1, Weight(64.0)),
        Two(2, Weight(51.4)),
        Three(3, Weight(37.5)),
        Four(4, Weight(74.0)),
    }
}

@Serializable
data class Weight(
    val value: Double,
    val unit: WeightMeasurementUnit = METRIC_TON,
) {
    fun toMetricTons(): Double = value * when (unit) {
        METRIC_TON -> 1.0
        SHORT_TON -> 0.90718474
        POUND -> 0.45359237 / 1000.0
    }

    fun toOsmString(): String = value.toShortString() + when (unit) {
        METRIC_TON -> ""
        SHORT_TON -> " st"
        POUND -> " lbs"
    }
}

fun MaxWeightRestriction.applyTo(tags: Tags) {
    val weight = weight?.toOsmString() ?: return
    when (this) {
        is MaxWeight -> tags["maxweight"] = weight
        is MaxWeightRating -> tags["maxweightrating"] = weight
        is MaxWeightRatingHgv -> tags["maxweightrating:hgv"] = weight
        is MaxAxleLoad -> tags["maxaxleload"] = weight
        is MaxTandemAxleLoad -> tags["maxbogieweight"] = weight
        is MaxWeightCategorySweden -> {
            tags["maxweight"] = weight
            // See discussion here: https://github.com/streetcomplete/StreetComplete/issues/6586
            // We add the specific traffic_sign codes save the context of this information
            // Also: do not overwrite any existing signs
            if (tags["traffic_sign"] == null) {
                tags["traffic_sign"] = "SE:CSS-" + category!!.value.toString()
            }
        }
    }
}
