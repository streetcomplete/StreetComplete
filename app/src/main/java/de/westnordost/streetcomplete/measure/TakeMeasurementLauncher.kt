package de.westnordost.streetcomplete.measure

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import de.westnordost.streetcomplete.measure.MeasureActivity.Companion.RESULT_MEASURE_IN_METERS
import de.westnordost.streetcomplete.util.ActivityForResultLauncher

/** Launches the MeasureActivity and returns its result */
class TakeMeasurementLauncher(caller: ActivityResultCaller) {

    private val activityForResultLauncher = ActivityForResultLauncher(caller, ActivityResultContracts.StartActivityForResult())

    /** Returns null or the exact measurement in meters */
    suspend operator fun invoke(
        context: Context,
        mode: MeasureActivity.MeasureMode? = null,
        unit: MeasureDisplayUnit? = null
    ): Float? {
        val result = activityForResultLauncher(MeasureActivity.createIntent(context, mode, unit, true))
        if (result.resultCode != Activity.RESULT_OK) return null
        return result.data?.getFloatExtra(RESULT_MEASURE_IN_METERS, -1f)?.takeIf { it != -1f }
    }
}
