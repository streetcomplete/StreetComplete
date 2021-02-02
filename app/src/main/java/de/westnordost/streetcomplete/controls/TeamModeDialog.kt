package de.westnordost.streetcomplete.controls

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.dialog_team_mode.view.*

/** Shows a dialog containing the team mode settings */
class TeamModeDialog(
    context: Context
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_team_mode, null)

        view.team_size_input.addTextChangedListener {
            val number = parseTeamSize(it.toString())
            view.team_size_hint.isGone = number !== null
        }

        setView(view)
    }

    private fun parseTeamSize(string: String): Int? {
        return try {
            val number = Integer.parseInt(string)
            if (number in 2..12) number else null
        } catch (e: NumberFormatException) { null }
    }
}
