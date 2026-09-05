package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun <T> OsmElementQuestType<T>.answerAppliedTo(answer: T, tags: Map<String, String>): Set<StringMapEntryChange> {
    val cb = StringMapChangesBuilder(tags)
    val localMidday = LocalDateTime(1970, 1, 1, 12, 0)
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
    applyAnswerTo(answer, cb, ElementPointGeometry(LatLon(0.0, 0.0)), localMidday)
    return cb.create().changes
}

fun <T> OsmElementQuestType<T>.answerApplied(answer: T): Set<StringMapEntryChange> =
    answerAppliedTo(answer, emptyMap())
