package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.label_block2
import de.westnordost.streetcomplete.resources.label_housenumber
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** Form to input a block + housenumber. Since there is no uniform look, there's no custom styling.
 *
 *  When the user didn't input the block yet and inputs something into the housenumber field, the
 *  block number will be filled with the suggestion.
 *  */
@Composable
fun BlockAndHouseNumberForm(
    value: BlockAndHouseNumber,
    onValueChange: (BlockAndHouseNumber) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: BlockAndHouseNumber? = null,
) {
    val inputStyle = MaterialTheme.typography.largeInput
    val labelStyle = MaterialTheme.typography.caption.copy(
        hyphens = Hyphens.Auto,
        textAlign = TextAlign.Center,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
    )

    val useBlockSuggestion = value.block.isEmpty() && !suggestion?.block.isNullOrEmpty()

    var blockInputHeightPx by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ProvideTextStyle(inputStyle) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.width(112.dp)) {
                    AnAddressNumberInput(
                        value = value.block,
                        onValueChange = { onValueChange(value.copy(block = it)) },
                        suggestion = suggestion?.block,
                        modifier = Modifier
                            .weight(1f)
                            .onSizeChanged { blockInputHeightPx = it.height },
                    )
                    BlockStepperButton(
                        value = if (useBlockSuggestion) suggestion.block else value.block,
                        onValueChange = { onValueChange(value.copy(block = it)) },
                        modifier = Modifier.width(48.dp).height(blockInputHeightPx.pxToDp())
                    )
                }
                Text("-")
                HouseNumberInput(
                    value = value.houseNumber,
                    onValueChange = {
                        onValueChange(value.copy(
                            houseNumber = it,
                            block = if (useBlockSuggestion) suggestion.block else value.block
                        ))
                    },
                    modifier = Modifier.width(192.dp),
                    suggestion = suggestion?.houseNumber
                )
            }
        }
        ProvideTextStyle(labelStyle) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),) {
                Text(stringResource(Res.string.label_block2))
                Text("-")
                Text(stringResource(Res.string.label_housenumber))
            }
        }
    }
}

@Composable @Preview
private fun BlockAndHouseNumberFormPreview() {
    var value by remember { mutableStateOf(BlockAndHouseNumber("", "")) }
    BlockAndHouseNumberForm(
        value = value,
        onValueChange = { value = it },
        suggestion = BlockAndHouseNumber("D", "12d"),
    )
}
