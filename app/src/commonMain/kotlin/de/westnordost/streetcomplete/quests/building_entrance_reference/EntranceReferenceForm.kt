package de.westnordost.streetcomplete.quests.building_entrance_reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.address.AnAddressNumberInput
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Form to input an entrance reference - either a flat range, a ref code or both */
@Composable
fun EntranceReferenceForm(
    value: EntranceReference?,
    onValueChange: (EntranceReference?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val selectableItems = remember {
            listOf(
                ReferenceCodeAndFlatRange(ReferenceCode(""), FlatRange("", "")),
                ReferenceCode(""),
                FlatRange("", "")
            )
        }
        DropdownButton(
            items = selectableItems,
            onSelectedItem = onValueChange,
            selectedItem = value,
            itemContent = { Text(stringResource(it.text)) },
        )
        if (value != null) {
            ProvideTextStyle(MaterialTheme.typography.largeInput) {
                EntranceReferenceInput(
                    value = value,
                    onValueChange = onValueChange,
                )
            }
        }
    }
}

private val EntranceReference.text: StringResource get() = when (this) {
    is ReferenceCodeAndFlatRange -> Res.string.quest_entrance_reference_select_flat_range_and_code
    is FlatRange -> Res.string.quest_entrance_reference_select_flat_range_only
    is ReferenceCode -> Res.string.quest_entrance_reference_select_code_only
}
