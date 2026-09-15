package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

class IosChangesetAutoCloser : ChangesetAutoCloser {
    override fun enqueue(delayInMilliseconds: Long) {
        // iOS cannot reliably schedule work after 30 minutes of inactivity.
        // The OSM server closes inactive changesets itself after an hour.
    }
}
