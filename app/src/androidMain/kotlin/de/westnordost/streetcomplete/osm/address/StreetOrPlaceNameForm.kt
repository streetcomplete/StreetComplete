package de.westnordost.streetcomplete.osm.address

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.town_silhouette
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.SelectButton
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.painterResource

/** Input form for street name associated with an address or place name if there is no named
 *  street. */
@Composable
fun StreetOrPlaceNameForm(
    value: StreetOrPlaceName,
    onValueChange: (StreetOrPlaceName) -> Unit,
    modifier: Modifier = Modifier,
    showSelect: Boolean = true,
    streetNameSuggestion: String? = null,
    placeNameSuggestion: String? = null,
) {
    val selections = remember { listOf(StreetName(""), PlaceName("")) }

    val textStyle = MaterialTheme.typography.largeInput.copy(textAlign = TextAlign.Center)

    Column(modifier = modifier) {
        if (showSelect) {
            SelectButton(
                items = selections,
                selectedItem = value,
                onSelectedItem = onValueChange,
                style = ButtonStyle.Text,
                enabled = value.name.isEmpty(),
                itemContent = {
                    Text(stringResource(
                        when (it) {
                            is StreetName -> R.string.quest_address_street_street_name_label
                            is PlaceName -> R.string.quest_address_street_place_name_label
                        }
                    ))
                }
            )
        }

        when (value) {
            is StreetName -> {
                NameInput(
                    value = value.name,
                    onValueChange = { onValueChange(StreetName(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    suggestion = streetNameSuggestion,
                    textStyle = textStyle,
                )
                Text(
                    text = stringResource(R.string.quest_address_street_hint2),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    style = MaterialTheme.typography.caption
                )
            }
            is PlaceName -> {
                NameInput(
                    value = value.name,
                    onValueChange = { onValueChange(PlaceName(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .paint(
                            painter = painterResource(Res.drawable.town_silhouette),
                            alignment = Alignment.BottomCenter,
                            contentScale = ContentScale.Inside,
                            alpha = 0.15f,
                        ),
                    suggestion = placeNameSuggestion,
                    textStyle = textStyle,
                )
            }
        }
    }
}

@Composable
private fun NameInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: String? = null,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = if (!suggestion.isNullOrEmpty()) { {
            BasicText(
                text = suggestion,
                style = textStyle.copy(color = textStyle.color.copy(alpha = 0.2f)),
                // so that the text aligns center, just like the actual text
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(maxFontSize = textStyle.fontSize)
            )
        } } else null,
        textStyle = textStyle,
    )
}
