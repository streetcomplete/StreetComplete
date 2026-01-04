package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class AnnotationMode { OPEN, CLOSE }

private data class StrokePoint(val x: Float, val y: Float)

private fun loadBitmapWithRotation(path: String): Bitmap? {
    val bitmap = BitmapFactory.decodeFile(path) ?: return null
    val exif = ExifInterface(path)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val rotation = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    return if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

@Composable
fun PhotoAnnotationScreen(
    state: OcrFlowState,
    photoPath: String?,
    onPhotoPathChange: (String?) -> Unit,
    onStateChange: (OcrFlowState) -> Unit,
    onContinueToVerification: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Current annotation mode (open = green, close = red)
    var annotationMode by rememberSaveable { mutableStateOf(AnnotationMode.OPEN) }

    // Current strokes for open and close regions
    val openStrokes = remember { mutableStateListOf<StrokePoint>() }
    val closeStrokes = remember { mutableStateListOf<StrokePoint>() }

    // Brush width in dp, converted to pixels for drawing (smaller for precision)
    val brushWidthDp = 12.dp
    val brushWidthPx = with(density) { brushWidthDp.toPx() }

    val currentDayGroup = state.currentDayGroup
    val ocrProcessor = remember { OcrProcessor() }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            onPhotoPathChange(photoFile!!.absolutePath)
            scope.launch {
                bitmap = withContext(Dispatchers.IO) {
                    loadBitmapWithRotation(photoFile!!.absolutePath)
                }
            }
        }
    }

    // Launch camera if no photo yet
    LaunchedEffect(photoPath) {
        if (photoPath == null) {
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFileName = "ocr_photo_${nowAsEpochMilliseconds()}.jpg"
            val file = File(directory, imageFileName)
            file.createNewFile()
            photoFile = file

            val photoUri = FileProvider.getUriForFile(
                context,
                context.getString(R.string.fileprovider_authority),
                file
            )
            cameraLauncher.launch(photoUri)
        } else {
            scope.launch {
                bitmap = withContext(Dispatchers.IO) {
                    loadBitmapWithRotation(photoPath)
                }
            }
        }
    }

    // Function to calculate bounding box from strokes
    fun calculateBoundingBox(strokes: List<StrokePoint>, imageWidth: Int, imageHeight: Int): RectF? {
        if (strokes.isEmpty() || canvasSize.width == 0 || canvasSize.height == 0) return null

        val minX = strokes.minOf { it.x }
        val maxX = strokes.maxOf { it.x }
        val minY = strokes.minOf { it.y }
        val maxY = strokes.maxOf { it.y }

        // Convert canvas coordinates to image coordinates
        val scaleX = imageWidth.toFloat() / canvasSize.width
        val scaleY = imageHeight.toFloat() / canvasSize.height

        return RectF(
            (minX * scaleX).coerceIn(0f, imageWidth.toFloat()),
            (minY * scaleY).coerceIn(0f, imageHeight.toFloat()),
            (maxX * scaleX).coerceIn(0f, imageWidth.toFloat()),
            (maxY * scaleY).coerceIn(0f, imageHeight.toFloat())
        )
    }

    // Function to process current annotation and move to next
    fun confirmCurrentAnnotation(skipRemaining: Boolean) {
        val loadedBitmap = bitmap ?: return
        val currentGroup = currentDayGroup ?: return

        val openRegion = calculateBoundingBox(openStrokes.toList(), loadedBitmap.width, loadedBitmap.height)
        val closeRegion = calculateBoundingBox(closeStrokes.toList(), loadedBitmap.width, loadedBitmap.height)

        scope.launch {
            // Run OCR on highlighted regions
            val openTimeRaw = openRegion?.let {
                ocrProcessor.extractNumbersFromRegion(loadedBitmap, it)
            }
            val closeTimeRaw = closeRegion?.let {
                ocrProcessor.extractNumbersFromRegion(loadedBitmap, it)
            }

            // Update annotation for current group
            val updatedAnnotations = state.annotations.toMutableList()
            val currentAnnotation = DayAnnotation(
                dayGroup = currentGroup,
                openRegion = openRegion,
                closeRegion = closeRegion,
                openTimeRaw = openTimeRaw,
                closeTimeRaw = closeTimeRaw
            )

            if (state.currentGroupIndex < updatedAnnotations.size) {
                updatedAnnotations[state.currentGroupIndex] = currentAnnotation
            } else {
                updatedAnnotations.add(currentAnnotation)
            }

            if (skipRemaining || state.isLastGroup) {
                // Go to verification
                onStateChange(state.copy(annotations = updatedAnnotations))
                onContinueToVerification()
            } else {
                // Move to next group
                onStateChange(
                    state.copy(
                        annotations = updatedAnnotations,
                        currentGroupIndex = state.currentGroupIndex + 1
                    )
                )
                // Clear strokes for next group
                openStrokes.clear()
                closeStrokes.clear()
                annotationMode = AnnotationMode.OPEN
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.quest_openingHours_ocr_annotate_title)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onBack) { BackIcon() } },
            actions = {
                // Retake photo button
                IconButton(onClick = {
                    onPhotoPathChange(null)
                    bitmap = null
                    openStrokes.clear()
                    closeStrokes.clear()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.quest_openingHours_ocr_retake))
                }
            }
        )

        if (bitmap != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            ) {
                val loadedBitmap = bitmap!!

                // Full-screen photo with annotation canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                ) {
                    // Photo background
                    androidx.compose.foundation.Image(
                        bitmap = loadedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Annotation canvas overlay
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(annotationMode) {
                                detectDragGestures(
                                    onDrag = { change, _ ->
                                        val point = StrokePoint(
                                            change.position.x,
                                            change.position.y
                                        )
                                        when (annotationMode) {
                                            AnnotationMode.OPEN -> openStrokes.add(point)
                                            AnnotationMode.CLOSE -> closeStrokes.add(point)
                                        }
                                    }
                                )
                            }
                    ) {
                        // Draw open strokes (green)
                        if (openStrokes.isNotEmpty()) {
                            val openPath = Path().apply {
                                openStrokes.forEachIndexed { index, point ->
                                    if (index == 0) {
                                        moveTo(point.x, point.y)
                                    } else {
                                        lineTo(point.x, point.y)
                                    }
                                }
                            }
                            drawPath(
                                path = openPath,
                                color = TrafficSignColor.Green.copy(alpha = 0.6f),
                                style = Stroke(
                                    width = brushWidthPx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // Draw close strokes (red)
                        if (closeStrokes.isNotEmpty()) {
                            val closePath = Path().apply {
                                closeStrokes.forEachIndexed { index, point ->
                                    if (index == 0) {
                                        moveTo(point.x, point.y)
                                    } else {
                                        lineTo(point.x, point.y)
                                    }
                                }
                            }
                            drawPath(
                                path = closePath,
                                color = TrafficSignColor.Red.copy(alpha = 0.6f),
                                style = Stroke(
                                    width = brushWidthPx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // Floating controls at bottom
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Day group indicator row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentDayGroup?.toDisplayString() ?: "",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.progress,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Open/Close toggle and Clear buttons in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Open time button (green)
                            Button(
                                onClick = { annotationMode = AnnotationMode.OPEN },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (annotationMode == AnnotationMode.OPEN) {
                                        TrafficSignColor.Green
                                    } else {
                                        MaterialTheme.colors.surface
                                    },
                                    contentColor = if (annotationMode == AnnotationMode.OPEN) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colors.onSurface
                                    }
                                )
                            ) {
                                Text(stringResource(R.string.quest_openingHours_ocr_open_time))
                            }

                            // Close time button (red)
                            Button(
                                onClick = { annotationMode = AnnotationMode.CLOSE },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (annotationMode == AnnotationMode.CLOSE) {
                                        TrafficSignColor.Red
                                    } else {
                                        MaterialTheme.colors.surface
                                    },
                                    contentColor = if (annotationMode == AnnotationMode.CLOSE) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colors.onSurface
                                    }
                                )
                            ) {
                                Text(stringResource(R.string.quest_openingHours_ocr_close_time))
                            }

                            // Clear button
                            Button(
                                onClick = {
                                    when (annotationMode) {
                                        AnnotationMode.OPEN -> openStrokes.clear()
                                        AnnotationMode.CLOSE -> closeStrokes.clear()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text(stringResource(R.string.quest_openingHours_ocr_clear))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Next/Done and Skip buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Next/Done button
                            Button(
                                onClick = { confirmCurrentAnnotation(skipRemaining = false) },
                                modifier = Modifier.weight(1f),
                                enabled = openStrokes.isNotEmpty() || closeStrokes.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (state.isLastGroup) {
                                        stringResource(R.string.quest_openingHours_ocr_done)
                                    } else {
                                        stringResource(R.string.quest_openingHours_ocr_next)
                                    }
                                )
                            }

                            // Skip button (only show if not last group)
                            if (!state.isLastGroup) {
                                Button(
                                    onClick = { confirmCurrentAnnotation(skipRemaining = true) },
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(stringResource(R.string.quest_openingHours_ocr_skip))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Loading or waiting for camera
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.quest_openingHours_ocr_waiting_for_photo))
            }
        }
    }
}
