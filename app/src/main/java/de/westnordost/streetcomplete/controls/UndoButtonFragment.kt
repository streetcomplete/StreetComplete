package de.westnordost.streetcomplete.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChange
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesController
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesSource
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementSource
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.ktx.popIn
import de.westnordost.streetcomplete.ktx.popOut
import de.westnordost.streetcomplete.quests.getHtmlQuestTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.FutureTask
import javax.inject.Inject

/** Fragment that shows (and hides) the undo button, based on whether there is anything to undo */
class UndoButtonFragment : Fragment(R.layout.fragment_undo_button),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var osmElementChangesController: OsmElementChangesController
    @Inject internal lateinit var osmElementSource: OsmElementSource
    @Inject internal lateinit var uploadProgressSource: UploadProgressSource
    @Inject internal lateinit var featureDictionaryFutureTask: FutureTask<FeatureDictionary>

    private val undoButton get() = view as ImageButton

    /* undo button is not shown when there is nothing to undo */
    private val osmElementChangesListener = object : OsmElementChangesSource.Listener {
        override fun onAddedChange(change: OsmElementChange) {
            launch(Dispatchers.Main) { animateInIfAnythingToUndo() }
        }
        override fun onSyncedChange(change: OsmElementChange) {
            launch(Dispatchers.Main) { animateOutIfNothingLeftToUndo() }
        }
        override fun onDeletedChange(change: OsmElementChange) {
            launch(Dispatchers.Main) { animateOutIfNothingLeftToUndo() }
        }
    }

    /* Don't allow undoing while uploading. Should prevent race conditions. (Undoing quest while
    *  also uploading it at the same time) */
    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { launch(Dispatchers.Main) { updateUndoButtonEnablement(false) }}
        override fun onFinished() { launch(Dispatchers.Main) { updateUndoButtonEnablement(true) }}
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        undoButton.setOnClickListener {
            undoButton.isEnabled = false
            val change = osmElementChangesController.getFirstUndoableChange()
            if (change != null) confirmUndo(change)
        }
    }

    override fun onStart() {
        super.onStart()
        updateUndoButtonVisibility()
        updateUndoButtonEnablement(true)
        osmElementChangesController.addListener(osmElementChangesListener)
        uploadProgressSource.addUploadProgressListener(uploadProgressListener)
    }

    override fun onStop() {
        super.onStop()
        osmElementChangesController.removeListener(osmElementChangesListener)
        uploadProgressSource.removeUploadProgressListener(uploadProgressListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun confirmUndo(change: OsmElementChange) {
        val ctx = context ?: return
        val element = osmElementSource.get(change.elementType, change.elementId)

        val inner = LayoutInflater.from(ctx).inflate(R.layout.dialog_undo, null, false)
        val icon = inner.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(change.questType.icon)
        val text = inner.findViewById<TextView>(R.id.text)
        text.text = resources.getHtmlQuestTitle(change.questType, element, featureDictionaryFutureTask)

        AlertDialog.Builder(ctx)
            .setTitle(R.string.undo_confirm_title)
            .setView(inner)
            .setPositiveButton(R.string.undo_confirm_positive) { _, _ ->
                osmElementChangesController.undoChange(change.id!!)
                updateUndoButtonEnablement(true)
            }
            .setNegativeButton(R.string.undo_confirm_negative) { _, _ -> updateUndoButtonEnablement(true) }
            .setOnCancelListener { updateUndoButtonEnablement(true) }
            .show()
    }

    private fun updateUndoButtonVisibility() {
        view?.isGone = osmElementChangesController.getFirstUndoableChange() == null
    }

    private fun updateUndoButtonEnablement(enable: Boolean) {
        undoButton.isEnabled = enable && !uploadProgressSource.isUploadInProgress
    }

    private fun animateInIfAnythingToUndo() {
        if (!undoButton.isVisible && osmElementChangesController.getFirstUndoableChange() != null) {
            undoButton.popIn()
        }
    }

    private fun animateOutIfNothingLeftToUndo() {
        if (undoButton.isVisible && osmElementChangesController.getFirstUndoableChange() == null) {
            undoButton.popOut().withEndAction { undoButton.visibility = View.INVISIBLE }
        }
    }
}
