package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

/** Controls downloading */
interface DownloadController {
    /** Download in at least the given bounding box asynchronously. The next-bigger rectangle
     * in a (z16) tiles grid that encloses the given bounding box will be downloaded.
     *
     * @param bbox the minimum area to download
     * @param isUserInitiated whether this shall be a priority download (cancels previous downloads
     *        and puts itself in the front)
     */
    fun download(bbox: BoundingBox, isUserInitiated: Boolean = false)
}
