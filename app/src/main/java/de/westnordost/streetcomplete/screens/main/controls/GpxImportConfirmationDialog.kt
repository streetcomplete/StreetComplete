package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.import.GpxImporter
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.util.getSelectedLocale
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** A dialog to confirm download of (potentially massive) data along imported GPX track */
class GpxImportConfirmationDialog(
    context: Context,
    importData: GpxImporter.GpxImportData,
    private val callback: (confirm: Boolean) -> Unit,
) : AlertDialog(context), KoinComponent {

    private val countryInfos: CountryInfos by inject()

    init {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_gpx_import_confirmation, null)
        setView(view)

        val lengthUnit = context?.let { getSelectedLocale(it) }?.country
            ?.let { countryInfos.get(listOf(it)) }?.lengthUnits?.first() ?: LengthUnit.METER
        val formattedArea = when (lengthUnit) {
            LengthUnit.FOOT_AND_INCH -> "%.0f acres".format(importData.areaToDownloadInSqkm * ACRES_IN_SQUARE_KILOMETER)
            else -> "%.1f km^2".format(importData.areaToDownloadInSqkm)
        }

        val downloadsToScheduleTextView = view.findViewById<TextView>(R.id.downloadsToScheduleText)
        downloadsToScheduleTextView.text = context.getString(
            R.string.gpx_import_number_of_downloads_to_schedule,
            importData.downloadBBoxes.size
        )
        val areaToDownloadTextView = view.findViewById<TextView>(R.id.areaToDownloadText)
        areaToDownloadTextView.text = context.getString(R.string.gpx_import_area_to_download, formattedArea)

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            callback(true)
            dismiss()
        }
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ ->
            cancel()
        }
    }

    companion object {
        private const val ACRES_IN_SQUARE_KILOMETER = 247.105
    }
}
