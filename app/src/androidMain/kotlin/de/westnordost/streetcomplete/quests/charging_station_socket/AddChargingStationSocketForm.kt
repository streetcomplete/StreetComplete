package de.westnordost.streetcomplete.quests.charging_station_socket

import android.os.Bundle
import android.view.View
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddChargingStationSocketForm :
    AbstractOsmQuestForm<Set<SocketType>>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    private val selectedItems = mutableStateOf(emptySet<SocketType>())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(R.string.quest_multiselect_hint))
                }

                ItemsSelectGrid(
                    columns = com.cheonjaeung.compose.grid.SimpleGridCells.Fixed(3),
                    items = SocketType.selectableValues,
                    selectedItems = selectedItems.value,
                    onSelect = { item, selected ->
                        if (!selected) {
                            selectedItems.value = selectedItems.value - item
                        } else {
                            selectedItems.value = selectedItems.value + item
                        }
                        checkIsFormComplete()
                    },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                ) {
                    ImageWithLabel(
                        painterResource(it.icon),
                        stringResource(it.title)
                    )
                }
            }
        }}
    }

    override fun onClickOk() {
        applyAnswer(selectedItems.value)
    }

    override fun isFormComplete() = selectedItems.value.isNotEmpty()
}
