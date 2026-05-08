package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/** Abstract base class for expandable and closeable bottom sheets. In detail, it does manage the
 *  behavior of the...
 *
 *  - bottom sheet
 *  - close/back button that is only shown when the bottom sheet is fully expanded
 */
abstract class AbstractBottomSheetFragment : Fragment(), IsCloseableBottomSheet {

    @UiThread
    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean = false

    /** Request to close the form through user interaction (back button, clicked other quest,..),
     * requires user confirmation if any changes have been made  */
    @UiThread
    override fun onClickClose(onConfirmed: () -> Unit) {
        if (!isRejectingClose()) {
            onDiscard()
            onConfirmed()
        } else {
            activity?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.confirmation_discard_title)
                    .setPositiveButton(R.string.confirmation_discard_positive) { _, _ ->
                        onDiscard()
                        onConfirmed()
                    }
                    .setNegativeButton(R.string.short_no_answer_on_button, null)
                    .show()
            }
        }
    }

    /** returns whether this form should not be closeable without confirmation */
    open fun isRejectingClose(): Boolean = false

    protected open fun onDiscard() {}
}
