package de.westnordost.streetcomplete.data.quest.atp

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.util.getNameAndLocationSpanned
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import kotlin.getValue

//see NoteDiscussionForm
class AtpCreateForm : AbstractQuestForm() {
    override val contentLayoutResId = R.layout.quest_atp_create
    private lateinit var entry: AtpEntry private set
    private val featureDictionaryLazy: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val featureDictionary: FeatureDictionary get() = featureDictionaryLazy.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = requireArguments()
        entry = Json.decodeFromString(args.getString(ATP_ENTRY)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setTitle(getString(osmElementQuestType.getTitle(element.tags)))
        setTitleHintLabel(getNameAndLocationSpanned(Node(
            1,
            position = entry.position,
            tags = entry.tagsInATP,
            version = 1,
            timestampEdited = 1,
        ), resources, featureDictionary))
        //setObjNote(element.tags["note"])
    }

    // include equivalents of
    //private val noteSource: NotesWithEditsSource by inject()
    //private val noteEditsController: NoteEditsController by inject()
    // to get ATP data from my API

    /*
    interface Listener {
        /** Called when the user successfully answered the quest */
        fun onNoteQuestSolved(questType: QuestType, noteId: Long, position: LatLon)
        /** Called when the user did not answer the quest but also did not hide it */
        fun onNoteQuestClosed()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener
     */

    companion object {
        private const val ATP_ENTRY = "atp_entry"

        fun createArguments(entry: AtpEntry) = bundleOf(
            ATP_ENTRY to Json.encodeToString(entry)
        )
    }
}
