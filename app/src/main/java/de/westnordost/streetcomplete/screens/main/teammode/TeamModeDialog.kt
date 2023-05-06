package de.westnordost.streetcomplete.screens.main.teammode

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import de.westnordost.streetcomplete.databinding.DialogTeamModeBinding
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeColorCircleView.Companion.MAX_TEAM_SIZE

/** Shows a dialog containing the team mode settings */
class TeamModeDialog(
    context: Context,
    onEnableTeamMode: (Int, Int) -> Unit
) : AlertDialog(context) {

    private var selectedTeamSize: Int? = null
    private var selectedIndexInTeam: Int? = null
    private val binding = DialogTeamModeBinding.inflate(LayoutInflater.from(context))

    init {
        val adapter = TeamModeIndexSelectAdapter()
        adapter.listeners.add(object : TeamModeIndexSelectAdapter.OnSelectedIndexChangedListener {
            override fun onSelectedIndexChanged(index: Int?) {
                selectedIndexInTeam = index
                updateOkButtonEnablement()
            }
        })
        binding.colorCircles.adapter = adapter
        binding.colorCircles.layoutManager = GridLayoutManager(context, 3)

        binding.teamSizeInput.doAfterTextChanged { editable ->
            selectedTeamSize = parseTeamSize(editable.toString())
            updateOkButtonEnablement()

            if (selectedTeamSize == null) {
                binding.introText.isGone = false
                binding.teamSizeHint.isGone = false
                binding.colorHint.isGone = true
                binding.colorCircles.isGone = true
            } else {
                binding.introText.isGone = true
                binding.teamSizeHint.isGone = true
                binding.colorHint.isGone = false
                binding.colorCircles.isGone = false
                adapter.count = selectedTeamSize!!
            }
        }

        setButton(BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
            onEnableTeamMode(selectedTeamSize!!, selectedIndexInTeam!!)
            dismiss()
        }

        setOnShowListener { updateOkButtonEnablement() }

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        setView(binding.root)
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
