package de.westnordost.streetcomplete.data.osmnotes

import java.util.Date
import de.westnordost.streetcomplete.data.Quest
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry

data class OsmNoteQuest(
    override var id: Long?,
    val note: Note,
    override var status: QuestStatus,
    var comment: String?,
    override var lastUpdate: Date,
    private val questType: OsmNoteQuestType,
    var imagePaths: List<String>?
) : Quest {

    constructor(note: Note, osmNoteQuestType: OsmNoteQuestType)
        : this(null, note, QuestStatus.NEW, null, Date(), osmNoteQuestType, null)

    override val type: QuestType<*> get() = questType
    override val markerLocations: Array<LatLon> get() = arrayOf(note.position)
    override val geometry: ElementGeometry get() = ElementPointGeometry(center)
    override val center: LatLon get() = note.position

    fun probablyContainsQuestion(): Boolean {
        /* from left to right (if smartass IntelliJ wouldn't mess up left-to-right):
           - latin question mark
           - greek question mark (a different character than semikolon, though same appearance)
           - semikolon (often used instead of proper greek question mark)
           - mirrored question mark (used in script written from right to left, like Arabic)
           - armenian question mark
           - ethopian question mark
           - full width question mark (often used in modern Chinese / Japanese)
           (Source: https://en.wikipedia.org/wiki/Question_mark)

            NOTE: some languages, like Thai, do not use any question mark, so this would be more
            difficult to determine.
       */
        val questionMarksAroundTheWorld = "[?;;؟՞፧？]"

        val text = note.comments?.firstOrNull()?.text
        return text?.matches(".*$questionMarksAroundTheWorld.*".toRegex()) ?: false
    }

    fun solve(text: String, imagePaths: List<String>?) {
        this.comment = text
        this.imagePaths = imagePaths
        status = QuestStatus.ANSWERED
    }

    fun hide() {
        status = QuestStatus.HIDDEN
    }

    fun close() {
        status = QuestStatus.CLOSED
    }
}
