package de.westnordost.streetcomplete.quests.charging_station_socket

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateMapOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.util.content

class AddChargingStationSocketForm :
    AbstractOsmQuestForm<Map<SocketType, Int>>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val counts = mutableStateMapOf<SocketType, Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content {
            Surface {
                SocketTypeAndCountForm(
                    counts = counts,
                    onCountsChanged = {
                        counts.clear()
                        counts.putAll(it)
                        checkIsFormComplete()
                    }
                )
            }
        }
    }

    override fun onClickOk() {
        if (counts.values.any { it !in 1..50 }) {
            confirmUnusualInput()
        } else {
            applyAnswer(counts.toMap())
        }
    }

    private fun confirmUnusualInput() {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_charging_station_socket_unusualInput_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    applyAnswer(counts.toMap())
                }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

    override fun isFormComplete() =
        counts.isNotEmpty()

    override fun isRejectingClose() =
        counts.isNotEmpty()
}
