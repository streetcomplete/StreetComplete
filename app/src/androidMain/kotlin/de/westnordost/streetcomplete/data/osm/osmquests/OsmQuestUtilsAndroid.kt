package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway
import de.westnordost.streetcomplete.quests.existence.CheckExistence
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.shop_type.CheckShopExistence

/** an index by which a list of quest types can be sorted so that quests that are the slowest to
 *  evaluate are evaluated first. This is a performance improvement because the evaluation is done
 *  in parallel on as many threads as there are CPU cores. So if all threads are done except one,
 *  all have to wait for that one thread. So, better enqueue the expensive work at the beginning. */
actual fun getAnalyzePriority(questType: OsmElementQuestType<*>): Int = when (questType) {
    is AddOpeningHours -> 0 // OpeningHoursParser, extensive filter
    is CheckExistence -> 1 // FeatureDictionary, extensive filter
    is CheckShopExistence -> 1 // FeatureDictionary, extensive filter
    is AddHousenumber -> 1 // complex filter
    is AddMaxHeight -> 1 // complex filter
    is AddCycleway -> 2 // complex filter
    is AddPlaceName -> 2 // FeatureDictionary, extensive filter
    else -> 10
}
