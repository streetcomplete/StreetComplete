package de.westnordost.streetcomplete.screens.measure

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters

class MeasureContract : ActivityResultContract<MeasureContract.Params, Length?>() {
    data class Params(val lengthUnit: LengthUnit, val measureVertical: Boolean)

    override fun createIntent(context: Context, input: Params): Intent {
        val unit = when (input.lengthUnit) {
            LengthUnit.METER -> DISPLAY_UNIT_METERS
            LengthUnit.FOOT_AND_INCH -> DISPLAY_UNIT_FT_IN
        }
        val precisionStep = when (input.lengthUnit) {
            LengthUnit.METER ->         10
            LengthUnit.FOOT_AND_INCH -> 4
        }
        val intent = Intent("de.westnordost.streetmeasure.MeasureActivity")
        val attributes = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorAccent))
        val argb = attributes.getColor(0, 0)
        attributes.recycle()
        intent.putExtras(bundleOf(
            PARAM_REQUEST_RESULT to true,
            PARAM_DISPLAY_UNIT to unit,
            PARAM_PRECISION_STEP to precisionStep,
            PARAM_MEASURE_VERTICAL to input.measureVertical,
            PARAM_ARROW_COLOR to argb
        ))
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Length? {
        if (resultCode != Activity.RESULT_OK) return null

        val meters = intent?.getDoubleExtra(RESULT_MEASURE_METERS, -1.0)?.takeIf { it != -1.0 }
        if (meters != null) {
            return LengthInMeters(meters)
        }

        val feet = intent?.getIntExtra(RESULT_MEASURE_FEET, -1)?.takeIf { it != -1 }
        val inches = intent?.getIntExtra(RESULT_MEASURE_INCHES, -1)?.takeIf { it != -1 }
        if (feet != null && inches != null) {
            return LengthInFeetAndInches(feet, inches)
        }
        return null
    }

    companion object {
        /* --------------------------------- Intent Parameters ---------------------------------- */

        /** Boolean. Whether to measure vertical distances. Default is to measure horizontal. */
        private const val PARAM_MEASURE_VERTICAL = "measure_vertical"

        /** String. Specifies which unit should be used for display and result returned.
        Either DISPLAY_UNIT_METERS or DISPLAY_UNIT_FT_IN. Default depends on the user's locale
         */
        private const val PARAM_DISPLAY_UNIT = "display_unit"

        private const val DISPLAY_UNIT_METERS = "meters"
        private const val DISPLAY_UNIT_FT_IN = "ft_in"

        /** Boolean. Whether this activity should return a result. If yes, the activity will return
         *  the measure result in RESULT_MEASURE_METERS or RESULT_MEASURE_FEET + RESULT_MEASURE_INCHES
         *  */
        private const val PARAM_REQUEST_RESULT = "request_result"

        /** Int. If PARAM_DISPLAY_UNIT = DISPLAY_UNIT_METERS, the centimeter steps to which the
         *  measure result is rounded during display and in the returned result. E.g. 10 means it is
         *  rounded to the decimeter.
         *  If PARAM_DISPLAY_UNIT = DISPLAY_UNIT_FT_IN, the inch steps to which the measure result
         *  is rounded during display and in the returned result. Only values between 1 and 12 are
         *  accepted. E.g. 12 means that it is rounded to the full foot.
         *
         *  For measuring widths along several meters (road widths), it is recommended to use 10cm
         *  / 4 inches, because a higher precision cannot be achieved on average with ARCore anyway
         *  and displaying the value in that precision may give a false sense that the measurement
         *  is that precise. */
        private const val PARAM_PRECISION_STEP = "display_precision"

        /** Int. Color value of the measurement arrow. By default is orange
         */
        private const val PARAM_ARROW_COLOR = "arrow_color"

        /* ----------------------------------- Intent Result ------------------------------------ */

        /** The action to identify a result */
        private const val RESULT_ACTION = "de.westnordost.streetmeasure.RESULT_ACTION"

        /** The result as displayed to the user, set if display unit was meters. Double. */
        private const val RESULT_MEASURE_METERS = "measure_result_meters"

        /** The result as displayed to the user, set if display unit was feet+inches. Int. */
        private const val RESULT_MEASURE_FEET = "measure_result_feet"

        /** The result as displayed to the user, set if display unit was feet+inches. Int. */
        private const val RESULT_MEASURE_INCHES = "measure_result_inches"
    }
}
