package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.updateCheckDate

fun BuildingType.applyToBuildingUse(tags: Tags) {
    require(osmKey != null && osmValue != null)
    if(osmValue == tags["building"]){
        tags.remove("building:use")
        return
    }
    tags["building:use"] = osmValue
}
