package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.address.BlockAndHouseNumber
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Form to input a block + housenumber. Since there is no uniform look, there's no styling.
 *
 *  When the user didn't input the block yet and applies the [suggestion] for the housenumber, the
 *  suggestion for the block will be applied, too. There are no stepper controls on the block
 *  because block (names) can be somewhat free-form */
@Composable
fun BlockAndHouseNumberForm(
    value: BlockAndHouseNumber,
    onValueChange: (BlockAndHouseNumber) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: BlockAndHouseNumber? = null,
) {
    val inputStyle = MaterialTheme.typography.largeInput.copy(textAlign = TextAlign.Center)
    val labelStyle = MaterialTheme.typography.caption.copy(
        hyphens = Hyphens.Auto,
        textAlign = TextAlign.Center,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.width(128.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField2(
                value = value.block,
                onValueChange = { onValueChange(value.copy(block = it)) },
                placeholder = { if (suggestion != null) {
                    BasicText(
                        text = suggestion.block,
                        style = inputStyle.copy(color = inputStyle.color.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(maxFontSize = inputStyle.fontSize)
                    )
                } },
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                textStyle = inputStyle,
                autoFitFontSize = true,
                singleLine = true,
            )
            Text(
                text = stringResource(R.string.label_block),
                style = labelStyle,
            )
        }
        Column(
            modifier = Modifier.width(192.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HouseNumberInput(
                value = value.houseNumber,
                onValueChange = { houseNumber, usedSuggestion ->
                    onValueChange(value.copy(
                        houseNumber = houseNumber,
                        block =
                            if (usedSuggestion && suggestion != null) suggestion.block
                            else value.block
                    ))
                },
                suggestion = suggestion?.houseNumber,
                textStyle = inputStyle,
            )
            Text(
                text = stringResource(R.string.label_housenumber),
                style = labelStyle,
            )
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
