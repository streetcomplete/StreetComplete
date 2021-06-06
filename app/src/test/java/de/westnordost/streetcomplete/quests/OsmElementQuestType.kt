package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange

import org.assertj.core.api.Assertions.*

fun <T> OsmElementQuestType<T>.verifyAnswer(tags:Map<String,String>, answer:T, vararg expectedChanges: StringMapEntryChange) {
    val cb = StringMapChangesBuilder(tags)
    this.applyAnswerTo(answer, cb)
    val changes = cb.create().changes
    assertThat(changes).containsExactlyInAnyOrder(*expectedChanges)
}

fun <T> OsmElementQuestType<T>.verifyAnswer(answer:T, vararg expectedChanges: StringMapEntryChange) {
    verifyAnswer(emptyMap(), answer, *expectedChanges)
}
