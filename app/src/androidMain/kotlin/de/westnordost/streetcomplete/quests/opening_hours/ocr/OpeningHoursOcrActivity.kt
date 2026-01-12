package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.ui.ktx.dir
import de.westnordost.streetcomplete.ui.theme.AppTheme

class OpeningHoursOcrActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    OpeningHoursOcrNavHost(
                        onComplete = { result -> finishWithResult(result) },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun finishWithResult(result: OcrOpeningHoursResult) {
        val intent = Intent().apply {
            putExtra(OpeningHoursOcrContract.EXTRA_RESULT, result)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

@Composable
private fun OpeningHoursOcrNavHost(
    onComplete: (OcrOpeningHoursResult) -> Unit,
    onCancel: () -> Unit
) {
    val navController = rememberNavController()
    val dir = LocalLayoutDirection.current.dir

    // Shared state across screens - OcrFlowState is Parcelable so rememberSaveable handles it automatically
    var flowState by rememberSaveable { mutableStateOf(OcrFlowState()) }
    var capturedPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    // Debug data for showing OCR results (not Parcelable due to Bitmap, so use remember)
    var debugDataList by remember { mutableStateOf<List<OcrDebugData>>(emptyList()) }

    fun goBack() {
        if (!navController.popBackStack()) onCancel()
    }

    NavHost(
        navController = navController,
        startDestination = OcrDestination.DayGrouping,
        enterTransition = { slideInHorizontally(initialOffsetX = { +it * dir }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it * dir }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it * dir }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { +it * dir }) }
    ) {
        composable(OcrDestination.DayGrouping) {
            DayGroupingScreen(
                state = flowState,
                onStateChange = { flowState = it },
                onContinue = { navController.navigate(OcrDestination.PhotoAnnotation) },
                onBack = ::goBack
            )
        }
        composable(OcrDestination.PhotoAnnotation) {
            PhotoAnnotationScreen(
                state = flowState,
                photoPath = capturedPhotoPath,
                onPhotoPathChange = { capturedPhotoPath = it },
                onStateChange = { flowState = it },
                onDebugDataReady = { data -> debugDataList = data },
                onContinueToDebug = { navController.navigate(OcrDestination.Debug) },
                onContinueToVerification = { navController.navigate(OcrDestination.Verification) },
                onBack = ::goBack
            )
        }
        composable(OcrDestination.Debug) {
            OcrDebugScreen(
                debugDataList = debugDataList,
                onContinue = { navController.navigate(OcrDestination.Verification) },
                onBack = ::goBack
            )
        }
        composable(OcrDestination.Verification) {
            VerificationScreen(
                state = flowState,
                onStateChange = { flowState = it },
                onApply = { result -> onComplete(result) },
                onBack = ::goBack
            )
        }
    }
}

private object OcrDestination {
    const val DayGrouping = "day_grouping"
    const val PhotoAnnotation = "photo_annotation"
    const val Debug = "debug"
    const val Verification = "verification"
}
