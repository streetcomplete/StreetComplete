package de.westnordost.streetcomplete.quests.clothing_bin_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddClothingBinOperatorForm : ANameWithSuggestionsForm<String>() {

    override val suggestions: List<String>? get() = countryInfo.clothesContainerOperators

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes with amenity = recycling and recycling_type = container").forEach {
            putMarker(it, R.drawable.ic_pin_recycling_container)
        }
    }

    override fun onClickOk() {
        applyAnswer(name)
    }
}
