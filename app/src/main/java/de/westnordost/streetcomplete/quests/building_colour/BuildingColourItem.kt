package de.westnordost.streetcomplete.quests.building_colour

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.FilteredDisplayItem

fun BuildingColour.asItem(context: Context): DisplayItem<BuildingColour> =
    BuildingColourDisplayItem(this, context)

class BuildingColourDisplayItem(buildingColour: BuildingColour, context: Context) :
    FilteredDisplayItem<BuildingColour>(buildingColour, context) {

    init {
        iconResId = R.drawable.ic_building_colour
    }
}
