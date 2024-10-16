package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType

fun <T> OsmElementQuestType<T>.answerAppliedTo(answer: T, tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    applyAnswerTo(answer, cb, ElementPointGeometry(LatLon(0.0, 0.0)), 0)
    return cb.create().changes
}

fun <T> OsmElementQuestType<T>.answerApplied(answer: T): Set<StringMapEntryChange> =
    answerAppliedTo(answer, emptyMap())
