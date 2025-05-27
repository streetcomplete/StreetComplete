package de.westnordost.streetcomplete.quests

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.ApplicationConstants.STREETMEASURE
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.screens.measure.ArQuestsDisabler
import de.westnordost.streetcomplete.screens.measure.MeasureContract
import de.westnordost.streetcomplete.util.ktx.isPackageInstalled
import de.westnordost.streetcomplete.util.ktx.openUri
import org.koin.android.ext.android.inject

/** Abstract superclass for all forms that let StreetMeasure measure stuff. */
abstract class AbstractArMeasureQuestForm<T> : AbstractOsmQuestForm<T>() {
    private val arQuestsDisabler: ArQuestsDisabler by inject()

    private val launcher = registerForActivityResult(MeasureContract(), ::onMeasuredInternal)

    private var requestedARInstall: Boolean = false
    private var requestedARMeasurement: Boolean = false

    override fun onResume() {
        super.onResume()
        // returned to app without StreetMeasure installed but requested install before
        if (requestedARInstall && !requireContext().packageManager.isPackageInstalled(STREETMEASURE)) {
            showDisableARMeasureQuestsDialog(R.string.quest_disable_message_not_installed)
        }
        requestedARInstall = false
        requestedARMeasurement = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(AR_INSTALL_REQUESTED, requestedARInstall)
        outState.putBoolean(AR_MEASUREMENT_REQUESTED, requestedARMeasurement)
    }

    fun takeMeasurement(lengthUnit: LengthUnit, measureVertical: Boolean) {
        try {
            requestedARMeasurement = true
            launcher.launch(MeasureContract.Params(lengthUnit, measureVertical))
        } catch (e: ActivityNotFoundException) {
            requestedARInstall = true
            requestedARMeasurement = false
            context?.openUri("market://details?id=$STREETMEASURE")
        }
    }

    private fun onMeasuredInternal(length: Length?) {
        // returned to app but StreetMeasure did not return a correct result (app crashed?)
        if (length == null) {
            showDisableARMeasureQuestsDialog(R.string.quest_disable_message_not_working)
            return
        }
        onMeasured(length)
    }

    protected abstract fun onMeasured(length: Length)

    private fun showDisableARMeasureQuestsDialog(@StringRes message: Int) {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_disable_title)
            .setMessage(
                ctx.getString(message) +
                "\n\n" +
                ctx.getString(R.string.quest_disable_message_tape_measure)
            )
            .setPositiveButton(R.string.quest_disable_action) { _, _ ->
                arQuestsDisabler.hideAllArQuests()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val AR_MEASUREMENT_REQUESTED = "ar_measurement_requested"
        private const val AR_INSTALL_REQUESTED = "ar_install_requested"
    }
}
