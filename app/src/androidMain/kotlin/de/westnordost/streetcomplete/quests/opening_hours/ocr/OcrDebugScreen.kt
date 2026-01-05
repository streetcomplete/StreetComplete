package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor

/**
 * Debug data for a single day group's OCR results.
 */
data class OcrDebugData(
    val dayGroup: DayGroup,
    val openCroppedBitmap: Bitmap? = null,
    val closeCroppedBitmap: Bitmap? = null,
    val openPreprocessedBitmap: Bitmap? = null,
    val closePreprocessedBitmap: Bitmap? = null,
    val openRawText: String = "",
    val closeRawText: String = "",
    val openParsedText: String = "",
    val closeParsedText: String = "",
    val isOpenAm: Boolean? = null,
    val isCloseAm: Boolean? = null,
    val isClosed: Boolean = false,
    val openConfidence: Float? = null,
    val closeConfidence: Float? = null
)

/**
 * Returns a color based on the confidence level.
 * Green for high confidence (>=80%), yellow for medium (>=60%), red for low (<60%).
 */
private fun getConfidenceColor(confidence: Float?): Color {
    return when {
        confidence == null -> Color.Gray
        confidence >= 0.8f -> Color(0xFF4CAF50)  // Green
        confidence >= 0.6f -> Color(0xFFFFC107)  // Yellow/Amber
        else -> Color(0xFFF44336)  // Red
    }
}

/**
 * Formats confidence as a percentage string.
 */
private fun formatConfidence(confidence: Float?): String {
    return if (confidence != null) {
        "${(confidence * 100).toInt()}%"
    } else {
        "N/A"
    }
}

/**
 * Debug screen that shows the cropped image regions and raw OCR text for each day group.
 * This is displayed after photo annotation and before verification to help debug OCR issues.
 */
@Composable
fun OcrDebugScreen(
    debugDataList: List<OcrDebugData>,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("OCR Debug") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onBack) { BackIcon() } }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(16.dp)
        ) {
            Text(
                text = "Review OCR Results",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "This screen shows the cropped regions and raw OCR text extracted from each region. Use this to debug OCR issues.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            debugDataList.forEachIndexed { index, debugData ->
                DebugDayGroupCard(debugData)
                if (index < debugDataList.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Continue button
        Column(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                )
                .padding(16.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.quest_openingHours_ocr_next))
            }
        }
    }
}

@Composable
private fun DebugDayGroupCard(data: OcrDebugData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = data.dayGroup.toDisplayString(),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )

            if (data.isClosed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Marked as CLOSED",
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Open time section
                Text(
                    text = "Open Time",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = TrafficSignColor.Green
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cropped bitmap preview
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cropped Region:",
                            style = MaterialTheme.typography.caption
                        )
                        if (data.openCroppedBitmap != null) {
                            Image(
                                bitmap = data.openCroppedBitmap.asImageBitmap(),
                                contentDescription = "Open time cropped region",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .border(1.dp, Color.Gray)
                                    .background(Color.White),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = "No region",
                                style = MaterialTheme.typography.body2,
                                color = Color.Red
                            )
                        }
                    }

                    // OCR results
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Raw OCR:",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = data.openRawText.ifEmpty { "(empty)" },
                            style = MaterialTheme.typography.body2,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .padding(4.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Parsed:",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = data.openParsedText.ifEmpty { "(empty)" },
                            style = MaterialTheme.typography.body2,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "AM/PM: ${when(data.isOpenAm) { true -> "AM"; false -> "PM"; null -> "not detected" }}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Confidence: ${formatConfidence(data.openConfidence)}",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = getConfidenceColor(data.openConfidence)
                        )
                    }
                }

                // Show preprocessed image if available
                if (data.openPreprocessedBitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Preprocessed (Adaptive Threshold):",
                        style = MaterialTheme.typography.caption
                    )
                    Image(
                        bitmap = data.openPreprocessedBitmap.asImageBitmap(),
                        contentDescription = "Preprocessed open time region",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color.Gray)
                            .background(Color.White),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close time section
                Text(
                    text = "Close Time",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = TrafficSignColor.Red
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cropped bitmap preview
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cropped Region:",
                            style = MaterialTheme.typography.caption
                        )
                        if (data.closeCroppedBitmap != null) {
                            Image(
                                bitmap = data.closeCroppedBitmap.asImageBitmap(),
                                contentDescription = "Close time cropped region",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .border(1.dp, Color.Gray)
                                    .background(Color.White),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = "No region",
                                style = MaterialTheme.typography.body2,
                                color = Color.Red
                            )
                        }
                    }

                    // OCR results
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Raw OCR:",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = data.closeRawText.ifEmpty { "(empty)" },
                            style = MaterialTheme.typography.body2,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .padding(4.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Parsed:",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = data.closeParsedText.ifEmpty { "(empty)" },
                            style = MaterialTheme.typography.body2,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "AM/PM: ${when(data.isCloseAm) { true -> "AM"; false -> "PM"; null -> "not detected" }}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Confidence: ${formatConfidence(data.closeConfidence)}",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = getConfidenceColor(data.closeConfidence)
                        )
                    }
                }

                // Show preprocessed image if available
                if (data.closePreprocessedBitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Preprocessed (Adaptive Threshold):",
                        style = MaterialTheme.typography.caption
                    )
                    Image(
                        bitmap = data.closePreprocessedBitmap.asImageBitmap(),
                        contentDescription = "Preprocessed close time region",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color.Gray)
                            .background(Color.White),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
