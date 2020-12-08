package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.util.TilesRect
import java.util.concurrent.atomic.AtomicBoolean

interface Downloader {
    var downloadedItemListener: OnDownloadedItemListener?

    fun download(tiles: TilesRect, cancelState: AtomicBoolean)
}
