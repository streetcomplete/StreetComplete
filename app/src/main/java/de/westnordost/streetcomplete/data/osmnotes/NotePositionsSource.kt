package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import javax.inject.Inject

/** Supplies a set of note positions, these are used to block the creation of other quests.
 *
 *  The note positions include the positions of OSM notes plus those notes that have been created
 *  locally but have not been uploaded yet. */
class NotePositionsSource @Inject constructor(
    private val noteDao: NoteDao,
    private val createNoteDao: CreateNoteDao
) {
    /** Get the positions of all notes within the given bounding box */
    fun getAllPositions(bbox: BoundingBox): List<LatLon> {
        return createNoteDao.getAllPositions(bbox) + noteDao.getAllPositions(bbox)
    }
}