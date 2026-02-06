package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_open_in_browser_24
import de.westnordost.streetcomplete.resources.weekly_osm_button
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateFormatter
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Dialog that notifies user of new edition of weeklyOSM */
@Composable
fun WeeklyOsmDialog(
    date: LocalDate,
    onDismissRequest: () -> Unit,
    onClickOpenWeeklyOsm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appear = remember { Animatable(0f) }

    LaunchedEffect(date) {
        appear.animateTo(1f, tween(2000, easing = LinearOutSlowInEasing))
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(null, null) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            Box(modifier
                .graphicsLayer {
                    val a = appear.value
                    rotationZ = a * 3400f
                    scaleX = a
                    scaleY = a
                    alpha = 0.6f + 0.4f * a
                }
                .background(
                    Brush.verticalGradient(0.8f to Color(0xffe0e0e0), 1.0f to Color(0xffbbbbbb))
                )
                .padding(24.dp)
            ) {
                val locale = Locale.current
                val dateFormatter = remember(locale) {
                    LocalDateFormatter(locale = locale, style = DateTimeFormatStyle.Full)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BasicText(
                        text = "weeklyOSM",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Serif
                        ),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased()
                    )
                    Divider(thickness = 2.dp, modifier = Modifier.padding(bottom = 2.dp))
                    Divider(thickness = 4.dp)
                    Text(dateFormatter.format(date))
                    Divider(thickness = 2.dp, modifier = Modifier.padding(bottom = 2.dp))
                    Row(
                        modifier = Modifier.height(360.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Column(Modifier.weight(2f)) {
                            BasicText(
                                text = stringResource(Res.string.weekly_osm_new_edition_title),
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased()
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                LoremIpsumLines(Modifier.weight(1f))
                                LoremIpsumLines(Modifier.weight(1f))
                            }
                        }
                        LoremIpsumLines(Modifier.weight(1f))
                    }
                }
                Button(
                    onClick = onClickOpenWeeklyOsm,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(Res.drawable.ic_open_in_browser_24), null)
                        Text(stringResource(Res.string.weekly_osm_button))
                    }
                }
            }
        }
    }
}
