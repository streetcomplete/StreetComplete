package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.action_open_in_browser
import de.westnordost.streetcomplete.resources.ic_open_in_browser_24
import de.westnordost.streetcomplete.resources.dialog_dont_notify_again
import de.westnordost.streetcomplete.resources.weekly_osm_new_edition_title
import de.westnordost.streetcomplete.ui.ktx.toPx
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
    onToggleDontNotifyAgain: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appear = remember { Animatable(0f) }
    val locale = Locale.current
    val dateFormatter = remember(locale) {
        LocalDateFormatter(locale = locale, style = DateTimeFormatStyle.Full)
    }

    LaunchedEffect(date) {
        appear.animateTo(1f, tween(1600, easing = LinearOutSlowInEasing))
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
            Newspaper(
                headline = "weeklyOSM",
                title = stringResource(Res.string.weekly_osm_new_edition_title),
                subtitle = dateFormatter.format(date),
                modifier = modifier
                    .width(320.dp)
                    .pointerInput(Unit) { detectTapGestures { /* nothing, just consume */ } }
                    .graphicsLayer {
                        val a = appear.value
                        rotationZ = a * 1803f
                        scaleX = a
                        scaleY = a
                        alpha = 0.4f + 0.6f * a
                    }
            ) {
                WeeklyOsmNewspaperContent(
                    date = date,
                    onClickOpenWeeklyOsm = onClickOpenWeeklyOsm,
                    onToggleDontShowAgain = onToggleDontNotifyAgain,
                    modifier = Modifier.rotate(-3f)
                )
            }
        }
    }
}

@Composable
private fun Newspaper(
    headline: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val t = 24.dp.toPx()
    Box(modifier
        .drawBehind {
            inset(t) {
                // some rotated papers in the background
                translate(top = -t / 2f) {
                    rotate(-6f) { drawRect(Color(0xffd9cec3)) }
                    rotate(3f) { drawRect(Color(0xffc7bbb4)) }
                }
                // gradient towards the bottom for a folded-newspaper look
                drawRect(Brush.verticalGradient(0.8f to Color(0xffe0e0e0), 1.0f to Color(0xffb0a199)))
            }
        }
        .padding(40.dp)
    ) {
        // newspaper background is grey (independent of light or black scheme), so content
        // color must be print-black
        CompositionLocalProvider(LocalContentColor provides Color.Black) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                BasicText(
                    text = headline,
                    style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased()
                )
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
                Divider(
                    thickness = 4.dp,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.body1.copy(fontFamily = FontFamily.Serif)
                )
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ProvideTextStyle(MaterialTheme.typography.body2.copy(fontSize = 6.sp, lineHeight = 10.sp)) {
                    Row(
                        modifier = Modifier.height(180.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(2f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BasicText(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                autoSize = TextAutoSize.StepBased()
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                LoremIpsumLines(Modifier.weight(1f).fillMaxHeight().alpha(0.1f))
                                LoremIpsumLines(Modifier.weight(1f).fillMaxHeight().alpha(0.1f))
                            }
                        }
                        LoremIpsumLines(Modifier.weight(1f).fillMaxHeight().alpha(0.1f))
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun BoxScope.WeeklyOsmNewspaperContent(
    date: LocalDate,
    onClickOpenWeeklyOsm: () -> Unit,
    onToggleDontShowAgain: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var dontShowAgain by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.align(Alignment.BottomCenter),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onClickOpenWeeklyOsm) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(Res.drawable.ic_open_in_browser_24), null)
                Text(stringResource(Res.string.action_open_in_browser))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = dontShowAgain,
                onCheckedChange = {
                    dontShowAgain = it
                    onToggleDontShowAgain(it)
                },
            )
            Text(
                text = stringResource(Res.string.dialog_dont_notify_again),
                style = MaterialTheme.typography.body1
            )
        }
    }
}
