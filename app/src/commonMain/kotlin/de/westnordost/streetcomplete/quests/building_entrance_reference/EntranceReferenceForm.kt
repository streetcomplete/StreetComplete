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
import de.westnordost.streetcomplete.quests.address.AnAddressNumberInput
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_entrance_reference_flat_range_label
import de.westnordost.streetcomplete.resources.quest_entrance_reference_flat_range_to
import de.westnordost.streetcomplete.resources.quest_entrance_reference_reference_label
import de.westnordost.streetcomplete.resources.quest_entrance_reference_select_code_only
import de.westnordost.streetcomplete.resources.quest_entrance_reference_select_flat_range_and_code
import de.westnordost.streetcomplete.resources.quest_entrance_reference_select_flat_range_only
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
        if (value is EntranceReference) {
            ProvideTextStyle(MaterialTheme.typography.largeInput) {
                when (value) {
                    is FlatRange -> {
                        FlatRangeInput(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.width(224.dp)
                        )
                    }
                    is ReferenceCode -> {
                        ReferenceCodeInput(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.width(128.dp)
                        )
                    }
                    is ReferenceCodeAndFlatRange -> {
                        Column(
                            modifier = Modifier,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val labelStyle = MaterialTheme.typography.caption.copy(
                                hyphens = Hyphens.Auto,
                                textAlign = TextAlign.Center,
                                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                            )
                            Text(
                                text = stringResource(Res.string.quest_entrance_reference_reference_label),
                                style = labelStyle,
                            )
                            ReferenceCodeInput(
                                value = value.referenceCode,
                                onValueChange = { onValueChange(value.copy(referenceCode = it)) },
                                modifier = Modifier.width(128.dp)
                            )
                            Text(
                                text = stringResource(Res.string.quest_entrance_reference_flat_range_label),
                                style = labelStyle,
                            )
                            FlatRangeInput(
                                value = value.flatRange,
                                onValueChange = { onValueChange(value.copy(flatRange = it)) },
                                modifier = Modifier.width(224.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlatRangeInput(
    value: FlatRange,
    onValueChange: (FlatRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnAddressNumberInput(
            value = value.start,
            onValueChange = { onValueChange(value.copy(start = it)) },
            modifier = Modifier.weight(0.5f)
        )
        Text(stringResource(Res.string.quest_entrance_reference_flat_range_to))
        AnAddressNumberInput(
            value = value.end,
            onValueChange = { onValueChange(value.copy(end = it)) },
            modifier = Modifier.weight(0.5f)
        )
    }
}

@Composable
private fun ReferenceCodeInput(
    value: ReferenceCode,
    onValueChange: (ReferenceCode) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnAddressNumberInput(
        value = value.value,
        onValueChange = { onValueChange(ReferenceCode(it)) },
        modifier = modifier,
    )
}

private val EntranceReference.text: StringResource get() = when (this) {
    is ReferenceCodeAndFlatRange -> Res.string.quest_entrance_reference_select_flat_range_and_code
    is FlatRange -> Res.string.quest_entrance_reference_select_flat_range_only
    is ReferenceCode -> Res.string.quest_entrance_reference_select_code_only
}
