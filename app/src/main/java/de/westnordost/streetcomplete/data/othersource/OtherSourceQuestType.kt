package de.westnordost.streetcomplete.data.othersource

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quests.questPrefix

/**
 * Very similar to OsmElementQuestType.
 *
 * Each quest type is responsible for downloading and persisting data, and uploading any changes if applicable.
 * ElementEdits are uploaded automatically, and onSyncedEdit is called after each uploaded ElementEdit.
 * Changes uploaded through [upload] may be things like reporting false positives for Osmose.
 *
 * [deleteMetadataOlderThan] is called every day and should be used to remove old data.
 */
// do it very similar to OsmElementQuestType
// for cleanup, each quest type should override deleteMetadataOlderThan, or old data will remain
interface OtherSourceQuestType : QuestType, ElementEditType {
    // like for OsmQuestType
    override val title: Int get() = getTitle(emptyMap())
    fun getTitle(tags: Map<String, String>): Int
    // getTitleArgs must contain the necessary amount of placeholders for the title string!
    // so override it if your title string contains %s, even if you replace the title in the form
    fun getTitleArgs(tags: Map<String, String>): Array<String> = arrayOf()
    val highlightedElementsRadius: Double get() = 30.0
    fun getHighlightedElements(getMapData: () -> MapDataWithGeometry): Sequence<Element> = emptySequence()
    val enabledInCountries: Countries get() = AllCountries

    /** Unique string for each source (app will crash on start if sources are not unique). */
    val source: String

    /**
     *  Download and persist data, create quests inside the given [bbox] and return the new quests.
     *  Download date should be stored for each entry to allow cleanup of old data.
     *  It's probably a good idea to remove old data inside the [bbox] before inserting updates.
     *
     *  Download will only happen if [downloadEnabled] is true.
     */
    fun download(bbox: BoundingBox): Collection<OtherSourceQuest>

    /**
     *  Upload changes to the server. Uploaded quests should not be created again on [download].
     *  Note that on each individual upload, [onSyncedEdit] will be called if there is a connected
     *  ElementEdit before finally [upload] is called.
     */
    fun upload()

    /** Return all quests inside the given [bbox]. */
    fun getQuests(bbox: BoundingBox): Collection<OtherSourceQuest>

    /** Return quest with the given [id], or null. */
    fun get(id: String): OtherSourceQuest?

    /**
     *  Called if an ElementEdit was done as part of solving the quest with the given [id].
     *  Actions should be taken so the quest for [id] does not appear again.
     */
    fun onAddedEdit(edit: ElementEdit, id: String)

    /**
     *  Called if the ElementEdit done as part of quest with the given [id] was deleted.
     *  This can be because an edit was undone (before or after upload), or because it was
     *  already uploaded and removed because it is older than MAX_UNDO_HISTORY_AGE.
     *  [id] can be null in case edit was not properly associated with id.
     */
    fun onDeletedEdit(edit: ElementEdit, id: String?)

    /**
     *  Called if the ElementEdit done as part of quest with the given [id] was synced (uploaded).
     *  [id] can be null in case edit was not properly associated with id.
     *  Note that [upload] will also be called (before the first edit upload).
     */
    fun onSyncedEdit(edit: ElementEdit, id: String?)

    /**
     *  Removes the quest with the given [id]. What happens internally doesn't matter, as long as
     *  the quest doesn't show up again when using [get] or [getQuests].
     */
    fun deleteQuest(id: String): Boolean

    /**
     *  Necessary to clean old data.
     *  Will be called with (nearly) current time when clearing all stored data is desired.
     */
    override fun deleteMetadataOlderThan(timestamp: Long)

    /** quest settings should always exist, at least to control [downloadEnabled] */
    override val hasQuestSettings get() = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog?

    /** quest settings should always exist, at least to disable downloading */
    var downloadEnabled: Boolean
        get() = prefs.getBoolean(downloadPref, false)
        set(value) = prefs.edit().putBoolean(downloadPref, value).apply()

    private val downloadPref get() = questPrefix(prefs) + "qs_${name}_enable_download"
}
