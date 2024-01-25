package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.streetcomplete.osm.opening_hours.parser.OpeningHoursRuleList

sealed interface CollectionTimesAnswer

data class CollectionTimes(val times: OpeningHoursRuleList) : CollectionTimesAnswer
data object NoCollectionTimesSign : CollectionTimesAnswer
