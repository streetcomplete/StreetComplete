package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

/** Closes changesets automatically after a certain time */
interface ChangesetAutoCloser {
    /** changesets are closed delayed after [delayInMilliseconds] of inactivity */
    fun enqueue(delayInMilliseconds: Long)
}
