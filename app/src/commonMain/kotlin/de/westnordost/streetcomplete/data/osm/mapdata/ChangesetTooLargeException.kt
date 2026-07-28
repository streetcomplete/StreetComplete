package de.westnordost.streetcomplete.data.osm.mapdata


/** While adding changes to our changeset, the API reports that the changeset limit is already
 *  reached. We must create a new changeset */
class ChangesetTooLargeException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)
