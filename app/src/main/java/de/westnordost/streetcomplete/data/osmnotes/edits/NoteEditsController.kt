package de.westnordost.streetcomplete.data.osmnotes.edits

import android.content.Context
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementIdUpdate
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class NoteEditsController(
    private val editsDB: NoteEditsDao
) : NoteEditsSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listeners = Listeners<NoteEditsSource.Listener>()

    fun add(
        noteId: Long,
        action: NoteEditAction,
        position: LatLon,
        text: String? = null,
        imagePaths: List<String> = emptyList(),
        track: List<Trackpoint> = emptyList(),
        isGpxNote: Boolean = false,
        context: Context? = null
    ) {
        val edit = NoteEdit(
            0,
            noteId,
            position,
            action,
            text,
            imagePaths,
            nowAsEpochMilliseconds(),
            false,
            imagePaths.isNotEmpty(),
            track,
        )
        if (isGpxNote) {
            createGpxNote(text ?: "", imagePaths, position, track, context)
        } else {
            synchronized(this) { editsDB.add(edit) }
            onAddedEdit(edit)
        }
    }

    fun get(id: Long): NoteEdit? =
        editsDB.get(id)

    override fun getAllUnsynced(): List<NoteEdit> =
        editsDB.getAllUnsynced()

    fun getAll(): List<NoteEdit> =
        editsDB.getAll()

    fun getOldestUnsynced(): NoteEdit? =
        editsDB.getOldestUnsynced()

    override fun getUnsyncedCount(): Int =
        editsDB.getUnsyncedCount()

    override fun getAllUnsyncedForNote(noteId: Long): List<NoteEdit> =
        editsDB.getAllUnsyncedForNote(noteId)

    override fun getAllUnsyncedForNotes(noteIds: Collection<Long>): List<NoteEdit> =
        editsDB.getAllUnsyncedForNotes(noteIds)

    override fun getAllUnsynced(bbox: BoundingBox): List<NoteEdit> =
        editsDB.getAllUnsynced(bbox)

    override fun getAllUnsyncedPositions(bbox: BoundingBox): List<LatLon> =
        editsDB.getAllUnsyncedPositions(bbox)

    fun getOldestNeedingImagesActivation(): NoteEdit? =
        editsDB.getOldestNeedingImagesActivation()

    fun markImagesActivated(id: Long): Boolean =
        synchronized(this) { editsDB.markImagesActivated(id) }

    fun markSynced(edit: NoteEdit, note: Note) {
        val markSyncedSuccess: Boolean
        synchronized(this) {
            if (edit.noteId != note.id) {
                editsDB.updateNoteId(edit.noteId, note.id)
            }
            markSyncedSuccess = editsDB.markSynced(edit.id)
        }

        if (markSyncedSuccess) {
            onSyncedEdit(edit)
        }
    }

    fun markSyncFailed(edit: NoteEdit): Boolean =
        delete(edit)

    fun undo(edit: NoteEdit): Boolean =
        delete(edit)

    fun deleteSyncedOlderThan(timestamp: Long): Int {
        val deletedCount: Int
        val deleteEdits: List<NoteEdit>
        synchronized(this) {
            deleteEdits = editsDB.getSyncedOlderThan(timestamp)
            if (deleteEdits.isEmpty()) return 0
            deletedCount = editsDB.deleteAll(deleteEdits.map { it.id })
        }
        onDeletedEdits(deleteEdits)
        return deletedCount
    }

    private fun delete(edit: NoteEdit): Boolean {
        val deleteSuccess = synchronized(this) { editsDB.delete(edit.id) }
        if (deleteSuccess) {
            onDeletedEdits(listOf(edit))
            return false
        }
        return true
    }

    fun updateElementIds(idUpdates: Collection<ElementIdUpdate>) {
        for (idUpdate in idUpdates) {
            val elementType = idUpdate.elementType.name.lowercase()
            editsDB.replaceTextInUnsynced(
                "osm.org/$elementType/${idUpdate.oldElementId} ",
                "osm.org/$elementType/${idUpdate.newElementId} ",
            )
        }
    }

    // there is some xmlwriter, and even gpxTrackWriter
    // maybe use this instead of the current ugly things, probably less prone to bugs caused by weird characters
    private fun createGpxNote(note: String, imagePaths: List<String>, position: LatLon, recordedTrack: List<Trackpoint>?, context: Context?) {
        val path = context?.getExternalFilesDir(null) ?: return
        path.mkdirs()
        val fileName = "notes.gpx"
        val gpxFile = File(path,fileName)
        if (gpxFile.createNewFile()) // if this file did not exist
            gpxFile.writeText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx \n" +
                " xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" +
                "</gpx>", Charsets.UTF_8)
        // now delete the last 6 characters, which is <\gpx>
        val oldText = gpxFile.readText(Charsets.UTF_8).dropLast(6)
        // save image file names (this is not nice, but better than not keeping any reference to them
        val imageText = if (imagePaths.isEmpty()) "" else
            "\n images used: ${imagePaths.joinToString(", ") { it.substringAfterLast(File.separator) }}"
        val trackFile: File?
        if (recordedTrack != null && recordedTrack.isNotEmpty()) {
            var i = 1
            while (File(path, "track_$i.gpx").exists()) {
                i += 1
            }
            trackFile = File(path, "track_$i.gpx")
            val formatter = DateTimeFormatter
                .ofPattern("yyyy_MM_dd'T'HH_mm_ss.SSSSSS'Z'")
                .withZone(ZoneOffset.UTC)
            val trackText = recordedTrack.map {
                "     <trkpt lon=\"${it.position.longitude}\" lat=\"${it.position.latitude}\">\n" +
                    "       <time>\"${formatter.format(Instant.ofEpochMilli(it.time))}\"</time>\n" +
                    if (it.elevation == 0.0f)
                        ""
                    else {
                        "       <ele>\"${it.elevation}\"</ele>\n" +
                            "       <hdop>\"${it.accuracy}\">\n"
                    } +
                    "     </trkpt>"
            }
            trackFile.writeText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx \n" +
                " xmlns=\"http://www.topografix.com/GPX/1/1\" \n" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" +
                "  <trk>\n" +
                "    <name>${trackFile.name.substringBefore(".gpx")}</name>\n" +
                "    <trkseg>\n" +
                trackText.joinToString("\n") + "\n" +
                "    </trkseg>\n" +
                "  </trk>\n" +
                "</gpx>", Charsets.UTF_8)
        } else trackFile = null
        val trackText = if (trackFile == null) "" else
            "\n attached track: ${trackFile.name}"
        gpxFile.writeText(oldText +" <wpt lon=\"" + position.longitude + "\" lat=\"" + position.latitude + "\">\n" +
            "  <name>" + (note + trackText + imageText).replace("&","&amp;")
            .replace("<","&lt;")
            .replace(">","&gt;")
            .replace("\"","&quot;")
            .replace("'","&apos;") + "</name>\n" +
            " </wpt>\n" +
            "</gpx>", Charsets.UTF_8)
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: NoteEditsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: NoteEditsSource.Listener) {
        listeners.remove(listener)
    }

    private fun onAddedEdit(edit: NoteEdit) {
        listeners.forEach { it.onAddedEdit(edit) }
    }

    private fun onSyncedEdit(edit: NoteEdit) {
        listeners.forEach { it.onSyncedEdit(edit) }
    }

    private fun onDeletedEdits(edits: List<NoteEdit>) {
        listeners.forEach { it.onDeletedEdits(edits) }
    }
}
