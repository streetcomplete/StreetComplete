package de.westnordost.streetcomplete.quests.charging_station_socket

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.util.content
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AddChargingStationSocketForm :
    AbstractOsmQuestForm<List<SocketCount>>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private var selectedTypes = mutableStateListOf<SocketType>()
    private var socketCounts = mutableStateListOf<SocketCount>()

    private val maxSockets = 50

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapshotFlow { socketCounts.toList() }
            .onEach { checkIsFormComplete() }
            .launchIn(lifecycleScope)

        binding.composeViewBase.content {
            Surface {
                ChargingSocketMultiStepForm(
                    selectedTypes = selectedTypes,
                    socketCounts = socketCounts,
                    onTypeSelected = { type ->
                        if (!selectedTypes.contains(type)) {
                            selectedTypes.add(type)
                        }
                    },
                    onTypeDeselected = { type ->
                        selectedTypes.remove(type)
                        socketCounts.removeAll { it.type == type }
                    },
                    onCountChanged = { type, count ->
                        socketCounts.removeAll { it.type == type }
                        socketCounts.add(SocketCount(type, count))
                    }
                )
            }
        }
    }

    override fun onClickOk() {
        if (socketCounts.any { it.count <= 0 || it.count > maxSockets }) {
            confirmUnusualInput {
                applyAnswer(socketCounts)
            }
        } else {
            applyAnswer(socketCounts)
        }
    }

    override fun isFormComplete(): Boolean =
        socketCounts.isNotEmpty() &&
            socketCounts.all { it.count > 0 }

    private fun confirmUnusualInput(callback: () -> Unit) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_maxweight_unusualInput_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }
}
