package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.testutils.assertSetsAreEqual

fun <T> OsmElementQuestType<T>.verifyAnswer(tags: Map<String, String>, answer: T, vararg expectedChanges: StringMapEntryChange) {
    val cb = StringMapChangesBuilder(tags)
    this.applyAnswerTo(answer, cb, ElementPointGeometry(LatLon(0.0, 0.0)), 0)
    assertSetsAreEqual(expectedChanges.toSet(), cb.create().changes)
}

fun <T> OsmElementQuestType<T>.verifyAnswer(answer: T, vararg expectedChanges: StringMapEntryChange) {
    verifyAnswer(emptyMap(), answer, *expectedChanges)
}
