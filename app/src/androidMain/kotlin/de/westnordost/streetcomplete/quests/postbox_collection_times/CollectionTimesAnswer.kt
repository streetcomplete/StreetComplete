package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours

sealed interface CollectionTimesAnswer

data class CollectionTimes(val times: HierarchicOpeningHours) : CollectionTimesAnswer
data object NoCollectionTimesSign : CollectionTimesAnswer
