package de.westnordost.streetcomplete.quests

import android.os.Bundle

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange
import junit.framework.TestCase

abstract class AOsmElementQuestTypeTest : TestCase() {

    protected abstract val questType: OsmElementQuestType

    protected var bundle = Bundle()
    protected var tags = mutableMapOf<String, String>()

    public override fun setUp() {
        super.setUp()
        bundle = Bundle()
        tags = mutableMapOf()
    }

    protected fun verify(vararg expectedChanges: StringMapEntryChange) {
        val cb = StringMapChangesBuilder(tags)
        questType.applyAnswerTo(bundle, cb)
        val changes = cb.create().changes
        for (expectedChange in expectedChanges) {
            assertTrue(changes.contains(expectedChange))
        }
    }
}
