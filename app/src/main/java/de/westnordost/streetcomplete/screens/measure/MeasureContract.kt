package de.westnordost.streetcomplete.screens.measure

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters

class MeasureContract : ActivityResultContract<MeasureContract.Params, Length?>() {
    data class Params(val lengthUnit: LengthUnit, val measureVertical: Boolean, )

    override fun createIntent(context: Context, input: Params): Intent {
        val unit = when (input.lengthUnit) {
            LengthUnit.METER -> "meter"
            LengthUnit.FOOT_AND_INCH -> "foot_and_inch"
        }
        val intent = context.packageManager.getLaunchIntentForPackage("de.westnordost.streetmeasure")
            ?: throw ActivityNotFoundException()
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        val attributes = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorAccent))
        val argb = attributes.getColor(0, 0)
        attributes.recycle()
        intent.putExtras(bundleOf(
            "request_result" to       true,
            "unit" to                 unit,
            "precision_cm" to         10,
            "precision_inch" to       4,
            "measure_vertical" to     input.measureVertical,
            "measuring_tape_color" to argb
        ))
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Length? {
        if (resultCode != Activity.RESULT_OK) return null

        val meters = intent?.getDoubleExtra("meters", -1.0)?.takeIf { it != -1.0 }
        if (meters != null) {
            return LengthInMeters(meters)
        }

        val feet = intent?.getIntExtra("feet", -1)?.takeIf { it != -1 }
        val inches = intent?.getIntExtra("inches", -1)?.takeIf { it != -1 }
        if (feet != null && inches != null) {
            return LengthInFeetAndInches(feet, inches)
        }
        return null
    }
}
