package de.westnordost.streetcomplete.quests.opening_hours

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.opening_hours.model.Weekdays
import java.util.Locale

object WeekdaysPickerDialog {

    fun show(context: Context, weekdays: Weekdays?, locale: Locale, callback: (Weekdays) -> Unit): AlertDialog {
        val selection = weekdays?.selection ?: BooleanArray(Weekdays.OSM_ABBR_WEEKDAYS.size)

        val localeWeekdayNames = Weekdays.getNames(context.resources, locale)
        val weekdayNames = Weekdays.getNames(context.resources, Locale.getDefault())

        val names = localeWeekdayNames.mapIndexed { index, localeWeekdayName ->
            val weekdayName = weekdayNames[index]
            localeWeekdayName + if (weekdayName != localeWeekdayName) " — $weekdayName" else ""
        }.toTypedArray()

        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.quest_openingHours_chooseWeekdaysTitle)
            .setMultiChoiceItems(names, selection) { _, _, _ -> }
            .setNeutralButton("Uncheck all") { _, _ ->  }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> callback(Weekdays(selection)) }

        val created = builder.create();
        created.show();

        // TODO: overriding the `Uncheck all` listener to keep it from closing isn't working currently.
        created.getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener { _ -> run {
                val view = created.listView;
                selection.forEachIndexed { i, _ -> view.setItemChecked(i, false) }
            }}

        return created;
    }
}
