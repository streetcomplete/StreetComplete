package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun BuildingType.applyToBuildingUse(tags: Tags) {
    require(osmKey != null && osmValue != null)
    // if user selects building:use same as building tag, remove redundant tagging
    if(osmValue == tags["building"]){
        tags.remove("building:use")
        return
    }
    tags["building:use"] = osmValue
    // we set the check_date:building:use and not check_date because this is about the secondary feature,
    // and updateCheckDateForKey() internally updates check_date if it existed before..
    if (!tags.hasChanges) {
        tags.updateCheckDateForKey("building:use")
    }
}
