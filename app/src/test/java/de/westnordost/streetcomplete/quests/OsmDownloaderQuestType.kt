package de.westnordost.streetcomplete.quests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquest.OsmDownloaderQuestType
import org.junit.Assert.fail

fun OsmDownloaderQuestType<*>.verifyDownloadYieldsNoQuest(bbox: BoundingBox) {
    download(bbox) { element, _ ->
        fail("Expected zero elements. Element returned: ${element.type.name}#${element.id}")
    }
}

fun OsmDownloaderQuestType<*>.verifyDownloadYieldsQuest(bbox: BoundingBox) {
    var hasQuest = false
    download(bbox) { _, _ -> hasQuest = true }
    if (!hasQuest) {
        fail("Expected nonzero elements. Elements not returned")
    }
}
