package de.westnordost.streetcomplete.data.download

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.util.TilesRect
import java.util.concurrent.atomic.AtomicBoolean

interface Downloader {
    fun download(bbox: BoundingBox, cancelState: AtomicBoolean)
}
