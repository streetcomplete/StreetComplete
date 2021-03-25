package de.westnordost.streetcomplete.edithistory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitWayAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.*
import de.westnordost.streetcomplete.quests.getHtmlQuestTitle
import kotlinx.coroutines.*
import java.util.concurrent.FutureTask
import javax.inject.Inject

class UndoDialog(
    context: Context,
    private val edit: Edit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    @Inject internal lateinit var mapDataSource: MapDataWithEditsSource
    @Inject internal lateinit var featureDictionaryFutureTask: FutureTask<FeatureDictionary>
    @Inject internal lateinit var editHistoryController: EditHistoryController

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        Injector.applicationComponent.inject(this)

        val resources = context.resources

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_undo, null, false)


        setTitle(R.string.undo_confirm_title2)
        setView(view)
        setButton(BUTTON_POSITIVE, resources.getText(R.string.undo_confirm_positive), null) { _, _ ->
            scope.launch(Dispatchers.IO) { editHistoryController.undo(edit) }
        }
        setButton(BUTTON_NEGATIVE, resources.getText(R.string.undo_confirm_negative), null, null)
    }

    override fun onStart() {
        super.onStart()

        val editIcon = findViewById<ImageView>(R.id.editIcon)!!
        val titleText = findViewById<TextView>(R.id.titleText)!!
        val descriptionContainer = findViewById<FrameLayout>(R.id.descriptionContainer)!!

        scope.launch {
            editIcon.setImageResource(edit.icon)
            titleText.text = edit.getTitle()
            descriptionContainer.addView(edit.descriptionView)
        }
    }

    override fun dismiss() {
        super.dismiss()
        scope.cancel()
    }

    suspend fun Edit.getTitle(): CharSequence = when(this) {
        is ElementEdit -> {
            val element = withContext(Dispatchers.IO) { mapDataSource.get(elementType, elementId) }
            context.resources.getHtmlQuestTitle(questType, element, featureDictionaryFutureTask)
        }
        is NoteEdit -> {
            context.resources.getText(when(action) {
                CREATE -> R.string.created_note_action_title
                COMMENT -> R.string.commented_note_action_title
            })
        }
        else -> throw IllegalArgumentException()
    }

    val Edit.descriptionView: View get() = when(this) {
        is ElementEdit -> {
            when(action) {
                is UpdateElementTagsAction -> ;
                is DeletePoiNodeAction -> ;
                is SplitWayAction -> ;
            }
        }
        is NoteEdit -> {
            val txt = TextView(context)
            txt.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            txt.text = text
            txt
        }
        else -> throw IllegalArgumentException()
    }
}

