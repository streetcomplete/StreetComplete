package de.westnordost.streetcomplete.view

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestSourceDialogLayoutBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Asks user if he was really on-site */
suspend fun confirmIsSurvey(context: Context): Boolean {
    if (dontShowAgain) return true
    return suspendCancellableCoroutine { cont ->
        val dialogBinding = QuestSourceDialogLayoutBinding.inflate(LayoutInflater.from(context))
        dialogBinding.checkBoxDontShowAgain.isGone = timesShown < 1

        AlertDialog.Builder(context)
            .setTitle(R.string.quest_source_dialog_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                ++timesShown
                dontShowAgain = dialogBinding.checkBoxDontShowAgain.isChecked
                if (cont.isActive) cont.resume(true)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                if (cont.isActive) cont.resume(false)
            }
            .setOnCancelListener {
                if (cont.isActive) cont.resume(false)
            }
            .show()
    }
}

// "static" values, i.e. persisted per application start
private var dontShowAgain = false
private var timesShown = 0
