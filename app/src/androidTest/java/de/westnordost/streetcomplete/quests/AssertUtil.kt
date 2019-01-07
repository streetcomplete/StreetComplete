package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler


import de.westnordost.osmapi.map.data.BoundingBox

import junit.framework.Assert.fail

object AssertUtil {
    fun verifyYieldsNoQuest(quest: OsmElementQuestType, bbox: BoundingBox) {
        quest.download(bbox, MapDataWithGeometryHandler { element, _ ->
            fail("Expected zero elements. Element returned: ${element.type.name}#${element.id}")
        })
    }

    fun verifyYieldsQuest(quest: OsmElementQuestType, bbox: BoundingBox) {
        var hasQuest = false
        quest.download(bbox, MapDataWithGeometryHandler { _, _ -> hasQuest = true })
        if (!hasQuest) {
            fail("Expected nonzero elements. Elements not returned")
        }
    }
}
