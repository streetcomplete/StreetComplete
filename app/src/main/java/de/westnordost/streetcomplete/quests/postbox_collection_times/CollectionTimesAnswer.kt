package de.westnordost.streetcomplete.quests.postbox_collection_times

sealed class CollectionTimesAnswer

data class CollectionTimes(val times:List<WeekdaysTimes>) : CollectionTimesAnswer()
object NoCollectionTimesSign : CollectionTimesAnswer()
