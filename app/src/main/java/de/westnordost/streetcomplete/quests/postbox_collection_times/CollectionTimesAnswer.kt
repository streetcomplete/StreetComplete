package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.osm_opening_hours.model.OpeningHours

sealed interface CollectionTimesAnswer

data class CollectionTimes(val times: OpeningHours) : CollectionTimesAnswer
data object NoCollectionTimesSign : CollectionTimesAnswer
