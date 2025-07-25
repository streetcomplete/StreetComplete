package de.westnordost.streetcomplete.quests.opening_hours

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.opening_hours.model.Months
import de.westnordost.streetcomplete.osm.opening_hours.model.getNames

object MonthsPickerDialog {

    fun show(context: Context, months: Months?, locale: Locale, callback: (Months) -> Unit): AlertDialog {
        val selection = months?.selection ?: BooleanArray(Months.MONTHS_COUNT)

        val localeMonthsNames = Months.getNames(locale)
        val monthsNames = Months.getNames()

        val names = localeMonthsNames.mapIndexed { index, localeMonthName ->
            val monthName = monthsNames[index]
            localeMonthName + if (monthName != localeMonthName) " — $monthName" else ""
        }.toTypedArray()

        return AlertDialog.Builder(context)
            .setTitle(R.string.quest_openingHours_chooseMonthsTitle)
            .setMultiChoiceItems(names, selection) { _, _, _ -> }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> callback(Months(selection)) }
            .show()
    }
}
