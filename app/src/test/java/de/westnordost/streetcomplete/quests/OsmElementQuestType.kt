package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange
import org.junit.Assert.fail

import org.assertj.core.api.Assertions.*

fun OsmElementQuestType<*>.verifyDownloadYieldsNoQuest(bbox: BoundingBox) {
    download(bbox) { element, _ ->
        fail("Expected zero elements. Element returned: ${element.type.name}#${element.id}")
    }
}

fun OsmElementQuestType<*>.verifyDownloadYieldsQuest(bbox: BoundingBox) {
    var hasQuest = false
    download(bbox) { _, _ -> hasQuest = true }
    if (!hasQuest) {
        fail("Expected nonzero elements. Elements not returned")
    }
}

fun <T> OsmElementQuestType<T>.verifyAnswer(tags:Map<String,String>, answer:T, vararg expectedChanges: StringMapEntryChange) {
    val cb = StringMapChangesBuilder(tags)
    this.applyAnswerTo(answer, cb)
    val changes = cb.create().changes
    assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}

fun <T> OsmElementQuestType<T>.verifyAnswer(answer:T, vararg expectedChanges: StringMapEntryChange) {
    verifyAnswer(emptyMap(), answer, *expectedChanges)
}
