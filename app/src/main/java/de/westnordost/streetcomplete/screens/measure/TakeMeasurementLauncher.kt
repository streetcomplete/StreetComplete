package de.westnordost.streetcomplete.screens.measure

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.screens.measure.MeasureActivity.Companion.RESULT_MEASURE_FEET
import de.westnordost.streetcomplete.screens.measure.MeasureActivity.Companion.RESULT_MEASURE_INCHES
import de.westnordost.streetcomplete.screens.measure.MeasureActivity.Companion.RESULT_MEASURE_METERS
import de.westnordost.streetcomplete.util.ActivityForResultLauncher

/** Launches the MeasureActivity and returns its result */
class TakeMeasurementLauncher(caller: ActivityResultCaller) {

    private val activityForResultLauncher = ActivityForResultLauncher(caller, ActivityResultContracts.StartActivityForResult())

    /** Returns the measured Length or null, displayed at and rounded to a precision of 10cm / 4in */
    suspend operator fun invoke(
        context: Context,
        lengthUnit: LengthUnit,
        measureVertical: Boolean? = null
    ): Length? {
        val unit = when (lengthUnit) {
            LengthUnit.METER -> MeasureDisplayUnitMeter(10)
            LengthUnit.FOOT_AND_INCH -> MeasureDisplayUnitFeetInch(4)
        }
        val result = activityForResultLauncher(MeasureActivity.createIntent(context, measureVertical, unit, true))
        if (result.resultCode != Activity.RESULT_OK) return null

        val meters = result.data?.getFloatExtra(RESULT_MEASURE_METERS, -1f)?.takeIf { it != -1f }
        if (meters != null) {
            /* e.g. `1.7f.toDouble()` will return `1.7000000476837158` but we really want the
               result as it is printed, this is why we first convert to string and then back to
               double :-/ */
            return LengthInMeters(meters.toString().toDouble())
        }

        val feet = result.data?.getIntExtra(RESULT_MEASURE_FEET, -1)?.takeIf { it != -1 }
        val inches = result.data?.getIntExtra(RESULT_MEASURE_INCHES, -1)?.takeIf { it != -1 }
        if (feet != null && inches != null) {
            return LengthInFeetAndInches(feet, inches)
        }
        return null
    }
}
