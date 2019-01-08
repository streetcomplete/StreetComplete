package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler

import de.westnordost.osmapi.map.data.BoundingBox
import org.junit.Assert.fail

fun OsmElementQuestType.verifyDownloadYieldsNoQuest(bbox: BoundingBox) {
    download(bbox, MapDataWithGeometryHandler { element, _ ->
        fail("Expected zero elements. Element returned: ${element.type.name}#${element.id}")
    })
}

fun OsmElementQuestType.verifyDownloadYieldsQuest(bbox: BoundingBox) {
    var hasQuest = false
    download(bbox, MapDataWithGeometryHandler { _, _ -> hasQuest = true })
    if (!hasQuest) {
        fail("Expected nonzero elements. Elements not returned")
    }
}
