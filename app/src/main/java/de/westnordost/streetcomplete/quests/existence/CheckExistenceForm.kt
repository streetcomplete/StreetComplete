package de.westnordost.streetcomplete.quests.existence

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class CheckExistenceForm : AbstractQuestAnswerFragment<Unit>() {

    override suspend fun addInitialMapMarkers() {
        /* put markers for objects that are exactly the same as for which this quest is asking for
           e.g. it's a ticket validator? -> display other ticket validators. Etc. */
        val feature = featureDictionary
            .byTags(osmElement!!.tags)
            .isSuggestion(false) // not brands
            .find()
            .firstOrNull() ?: return
        /* Not going to draw an icon for each and every one of those, we'll just use an icon for
           those we already have an icon for, for others, just a dot is displayed. */
        val icon = getIconByPresetId(feature.id)
        getMapData().filter { it.tags.containsAll(feature.tags) }.forEach {
            putMarker(it, icon)
        }
    }

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { deletePoiNode() },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(Unit) }
    )
}

private fun getIconByPresetId(id: String) = when(id) {
    "amenity/atm" -> R.drawable.ic_pin_money
    "amenity/bench" -> R.drawable.ic_pin_bench
    "amenity/clock" -> R.drawable.ic_pin_clock
    "amenity/drinking_water" -> R.drawable.ic_pin_water
    "amenity/post_box" -> R.drawable.ic_pin_mail
    "amenity/public_bookcase" -> R.drawable.ic_pin_book
    "amenity/recycling_container" -> R.drawable.ic_pin_recycling_container
    "amenity/telephone",
    "emergency/phone"-> R.drawable.ic_pin_phone
    "amenity/toilets"-> R.drawable.ic_pin_toilets
    "amenity/waste_basket" -> R.drawable.ic_pin_bin
    "leisure/picnic_table" -> R.drawable.ic_pin_picnic_table
    "man_made/surveillance/camera" -> R.drawable.ic_pin_surveillance_camera
    "tourism/information/board",
    "tourism/information/terminal",
    "tourism/information/map" -> R.drawable.ic_pin_information
    else -> null
}

private fun <X,Y> Map<X,Y>.containsAll(other: Map<X,Y>) = other.all { this[it.key] == it.value }
