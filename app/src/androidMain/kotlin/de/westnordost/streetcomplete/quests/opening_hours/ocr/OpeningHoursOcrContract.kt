package de.westnordost.streetcomplete.quests.opening_hours.ocr

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * ActivityResult contract for launching the Opening Hours OCR flow.
 *
 * Input: Unit (no input required)
 * Output: OcrOpeningHoursResult? (null if cancelled)
 */
class OpeningHoursOcrContract : ActivityResultContract<Unit, OcrOpeningHoursResult?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, OpeningHoursOcrActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): OcrOpeningHoursResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(EXTRA_RESULT)
    }

    companion object {
        const val EXTRA_RESULT = "ocr_opening_hours_result"
    }
}
