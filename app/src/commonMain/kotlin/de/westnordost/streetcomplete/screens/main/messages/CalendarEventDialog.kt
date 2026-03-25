package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmcal.CalendarEvent
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import de.westnordost.streetcomplete.util.locale.DateTimeFormatStyle
import de.westnordost.streetcomplete.util.locale.LocalDateFormatter
import de.westnordost.streetcomplete.util.locale.LocalTimeFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.floor

/** Dialog that shows an OSM event in the area */
@Composable
fun CalendarEventDialog(
    event: CalendarEvent,
    onDismissRequest: () -> Unit,
    onClickOpenEvent: () -> Unit,
    onToggleDontNotifyAgain: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val appear = remember { Animatable(0f) }

    LaunchedEffect(event) {
        appear.animateTo(1f, tween(600, easing = LinearOutSlowInEasing))
    }

    val locale = Locale.current
    val dateFormatter = remember(locale) {
        LocalDateFormatter(locale = locale, style = DateTimeFormatStyle.Medium)
    }
    val dateTime = event.startDate.toLocalDateTime()
    val formattedDateWithoutYear = dateFormatter
        .format(dateTime.date)
        .replace(dateTime.date.year.toString(), "")
        .trim(',', ' ')

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
            Calendar(
                modifier = modifier
                    .width(300.dp)
                    .pointerInput(Unit) { detectTapGestures { /* nothing, just consume */ } }
                    .graphicsLayer {
                        val a = appear.value
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        alpha = 0.2f + a * 0.8f
                        translationY = (1f - a) * 400 * density
                        rotationZ = (1f - a) * -30f
                    }
            ) {
                CalendarPage(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.calendar_new_event_title),
                                style = MaterialTheme.typography.body2
                            )
                            Text(
                                text = formattedDateWithoutYear,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                ) {
                    CalendarPageContent(
                        event = event,
                        onClickOpenEvent = onClickOpenEvent,
                        onToggleDontShowAgain = onToggleDontNotifyAgain
                    )
                }
            }
        }
    }
}

@Composable
private fun Calendar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val wirePainter = painterResource(Res.drawable.wire_binding)
    Box(
        modifier = modifier
            .drawWithContent {
                drawContent()
                // wire binding at top
                val wireWidth = wirePainter.intrinsicSize.width
                val repetitions = floor(size.width / wireWidth).toInt()
                val padding = (size.width - repetitions * wireWidth) / 2
                for (i in 0..<repetitions) {
                    translate(left = padding + i * wireWidth) {
                        with(wirePainter) { draw(wirePainter.intrinsicSize) }
                    }
                }
                // pages at bottom
                inset(top = size.height - 24.dp.toPx(), bottom = 0f, left = 0f, right = 0f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color(0xFF666666),
                            1f to Color(0xFFFFFFFF),
                            endY = 0.2f * 24.dp.toPx(),
                            tileMode = TileMode.Repeated
                        )
                    )
                }
            }
            .padding(top = 24.dp, bottom = 24.dp)
    ) {
        content()
    }
}

@Composable
private fun CalendarPage(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xffee2c2c))
                .padding(top = 32.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                title()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.Black) {
                content()
            }
        }
    }
}

@Composable
private fun CalendarPageContent(
    event: CalendarEvent,
    onClickOpenEvent: () -> Unit,
    onToggleDontShowAgain: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dontShowAgain by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.location_pin_red),
                    contentDescription = null
                )
                Text(
                    text = event.venue
                        ?: "${event.position.latitude.format(4)}, ${event.position.longitude.format(4)}",
                    style = MaterialTheme.typography.body1,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = onClickOpenEvent) {
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
}
