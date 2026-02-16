package de.westnordost.streetcomplete.quests.charging_station_socket

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.util.content
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddChargingStationSocketForm :
    AbstractOsmQuestForm<Map<SocketType, Int>>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val socketCounts = mutableStateMapOf<SocketType, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SocketType.selectableValues.forEach {
            socketCounts[it] = 0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapshotFlow { socketCounts.values.sum() }
            .onEach { checkIsFormComplete() }
            .launchIn(lifecycleScope)

        binding.composeViewBase.content {
            Surface {
                SocketTypeAndCountForm(
                    counts = socketCounts,
                    onIncrement = { type ->
                        val current = socketCounts[type] ?: 0
                        if (current < 50) socketCounts[type] = current + 1
                    },
                    onDecrement = { type ->
                        val current = socketCounts[type] ?: 0
                        if (current > 0) socketCounts[type] = current - 1
                    }
                )
            }
        }
    }

    override fun onClickOk() {
        if (hasUnusualInput()) {
            confirmUnusualInput { applyAnswer(socketCounts.toMap()) }
        } else {
            applyAnswer(socketCounts.toMap())
        }
    }

    private fun hasUnusualInput(): Boolean {
        return socketCounts.values.any { it == 0 || it > 50 }
    }

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

    override fun isFormComplete(): Boolean =
        socketCounts.values.any { it > 0 }

    override fun isRejectingClose(): Boolean =
        socketCounts.values.any { it > 0 }
}
