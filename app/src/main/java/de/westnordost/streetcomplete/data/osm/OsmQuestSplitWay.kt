package de.westnordost.streetcomplete.data.osm

import de.westnordost.streetcomplete.data.osm.changes.SplitWay

data class OsmQuestSplitWay(
    val id: Long,
    val questType: OsmElementQuestType<*>,
    val wayId: Long,
    val source: String,
    val splits: List<SplitWay>)
