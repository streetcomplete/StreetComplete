package de.westnordost.streetcomplete.controls

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.controls.TeamModeColorCircleView.Companion.MAX_TEAM_SIZE
import kotlinx.android.synthetic.main.dialog_team_mode.view.*

/** Shows a dialog containing the team mode settings */
class TeamModeDialog(
    context: Context,
    onEnableTeamMode: (Int, Int) -> Unit
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    private var selectedTeamSize: Int? = null
    private var selectedIndexInTeam: Int? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_team_mode, null)

        val adapter = TeamModeIndexSelectAdapter()
        adapter.listeners.add(object : TeamModeIndexSelectAdapter.OnSelectedIndexChangedListener {
            override fun onSelectedIndexChanged(index: Int?) {
                selectedIndexInTeam = index
                updateOkButtonEnablement()
            }
        })
        view.colorCircles.adapter = adapter
        view.colorCircles.layoutManager = GridLayoutManager(context, 3)

        view.teamSizeInput.addTextChangedListener { editable ->
            selectedTeamSize = parseTeamSize(editable.toString())
            updateOkButtonEnablement()

            if (selectedTeamSize == null) {
                view.introText.isGone = false
                view.teamSizeHint.isGone = false
                view.colorHint.isGone = true
                view.colorCircles.isGone = true
            } else {
                view.introText.isGone = true
                view.teamSizeHint.isGone = true
                view.colorHint.isGone = false
                view.colorCircles.isGone = false
                adapter.count = selectedTeamSize!!
            }
        }

        setButton(BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
            onEnableTeamMode(selectedTeamSize!!, selectedIndexInTeam!!)
            dismiss()
        }

        setOnShowListener { updateOkButtonEnablement() }

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        setView(view)
    }

    private fun updateOkButtonEnablement() {
        getButton(BUTTON_POSITIVE)?.isEnabled = selectedTeamSize != null && selectedIndexInTeam != null
    }

    private fun parseTeamSize(string: String): Int? {
        return try {
            val number = Integer.parseInt(string)
            if (number in 2..MAX_TEAM_SIZE) number else null
        } catch (e: NumberFormatException) { null }
    }
}

