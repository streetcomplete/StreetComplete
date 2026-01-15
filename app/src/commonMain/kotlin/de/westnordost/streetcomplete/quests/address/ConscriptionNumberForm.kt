package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.address.ConscriptionNumber
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_housenumber_conscription_number
import de.westnordost.streetcomplete.resources.quest_housenumber_street_number_optional
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.largeInput
import de.westnordost.streetcomplete.ui.theme.trafficSignContentColorFor
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** Form to input a conscription number + optional orientation number.
 *
 *  For Slovakia, the input fields are made to resemble how the signs usually look but not for Czech
 *  Republic, because there is no consistent look, compare [Prague](https://commons.wikimedia.org/wiki/Category:Conscription_and_orientation_house_number_in_Prague)
 *  with [Brno](https://commons.wikimedia.org/wiki/Category:Conscription_and_orientation_house_number_in_Brno),
 *  or [generally](https://commons.wikimedia.org/wiki/Category:Conscription_and_orientation_house_number_in_Czechia)
 *  and because the housenumber quest is disabled in Czech Republic anyway.
 *  */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConscriptionNumberForm(
    value: ConscriptionNumber,
    onValueChange: (ConscriptionNumber) -> Unit,
    countryCode: String?,
    modifier: Modifier = Modifier,
) {
    val inputStyle = MaterialTheme.typography.largeInput
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
            modifier = Modifier.widthIn(max = 128.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ConscriptionNumberSign(
                countryCode = countryCode,
                modifier = Modifier.width(96.dp),
            ) {
                /* Even though conscription numbers are always numbers, we don't force digit-only
                 * input here, because on conscription number plates, one will sometimes find a
                 * number like "I. 1234", which also specifies the subdivision number or something
                 * like that */
                ProvideTextStyle(inputStyle) {
                    AnAddressNumberInput(
                        value = value.conscriptionNumber,
                        onValueChange = { onValueChange(value.copy(conscriptionNumber = it)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                }
            }
            Text(
                text = stringResource(Res.string.quest_housenumber_conscription_number),
                style = labelStyle,
            )
        }
        Column(
            modifier = Modifier.widthIn(max = 128.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OrientationNumberSign(
                countryCode = countryCode,
                modifier = Modifier.width(96.dp),
            ) {
                /*
                As orientation numbers are basically normal house numbers, it may suggests itself to
                also offer the steppers and suggestions for user convenience. At this time, I
                decided against it because the user needs to use the keyboard anyway to fill in the
                conscription number, so this would only add little convenience and might even cause
                confusion as when the suggestion is shown, it might suggest that this field is
                mandatory rather than optional
                */
                ProvideTextStyle(inputStyle) {
                    AnAddressNumberInput(
                        value = value.streetNumber.orEmpty(),
                        onValueChange = {
                            onValueChange(value.copy(streetNumber = it.takeIf { it.isNotBlank() }))
                        },
                    )
                }
            }

            Text(
                text = stringResource(Res.string.quest_housenumber_street_number_optional),
                style = labelStyle,
            )
        }
    }
}

@Composable
private fun ConscriptionNumberSign(
    countryCode: String?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    when (countryCode) {
        "SK" -> SlovakHouseNumberSign(
            modifier = modifier,
            color = TrafficSignColor.Black,
            content = content,
        )
        else -> Box(modifier = modifier, content = content)
    }
}

@Composable
private fun OrientationNumberSign(
    countryCode: String?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    when (countryCode) {
        "SK" -> SlovakHouseNumberSign(
            modifier = modifier,
            color = TrafficSignColor.Red,
            content = content,
        )
        else -> Box(modifier = modifier, content = content)
    }
}

@Composable
private fun SlovakHouseNumberSign(
    modifier: Modifier = Modifier,
    color: Color = TrafficSignColor.Black,
    content: @Composable BoxScope.() -> Unit
) {
    val contentColor = trafficSignContentColorFor(color)
    Box(
        modifier = modifier
            .border(Dp.Hairline, Color.LightGray, RectangleShape)
            .background(color, RectangleShape)
            .padding(6.dp)
            .background(contentColor, CutCornerShape(12.dp))
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            content()
        }
    }
}

@Composable @Preview
private fun ConscriptionNumberFormPreview() {
    var value by remember { mutableStateOf(ConscriptionNumber("1234", null)) }
    ConscriptionNumberForm(
        value = value,
        onValueChange = { value = it },
        countryCode = "SK"
    )
}
