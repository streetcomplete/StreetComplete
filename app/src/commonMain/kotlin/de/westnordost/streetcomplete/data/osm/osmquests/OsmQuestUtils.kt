package de.westnordost.streetcomplete.data.osm.osmquests

/** an index by which a list of quest types can be sorted so that quests that are the slowest to
 *  evaluate are evaluated first. This is a performance improvement because the evaluation is done
 *  in parallel on as many threads as there are CPU cores. So if all threads are done except one,
 *  all have to wait for that one thread. So, better enqueue the expensive work at the beginning. */
expect fun getAnalyzePriority(questType: OsmElementQuestType<*>): Int
