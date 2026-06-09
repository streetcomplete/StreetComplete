package de.westnordost.streetcomplete.ui.util.measure

import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.ui.util.measure.ArMeasureResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

actual class ArMeasureAppLauncher {
    actual suspend fun measure(lengthUnit: LengthUnit, measureVertical: Boolean): ArMeasureResult {
        return ArMeasureResult.Error

        // As StreetMeasure doesn't exist on iOS, no point in trying to launch it there. However, if
        // it one day exists, we can try the following approach (warning, needs testing):

        /*
        val callbackScheme = streetcomplete://measureResult // need to register this in the Info.plist!!!
        val targetScheme = "streetmeasure://x-callback-url/measure"

        val unit = when (lengthUnit) {
            LengthUnit.METER -> "meter"
            LengthUnit.FOOT_AND_INCH -> "foot_and_inch"
        }
        val parameters = mapOf(
            "x-source" to             "StreetComplete",
            "x-success" to            callbackScheme,
            "x-cancel" to             callbackScheme,
            "unit" to                 unit,
            "precision_cm" to         10,
            "precision_inch" to       4,
            "measure_vertical" to     measureVertical,
        )
        val parametersString = parameters.entries.joinToString("&") { it.key + "=" + it.value }
        // Construct the x-callback-url parameters
        val urlString = "targetScheme?$parametersString"

        val url = NSURL.URLWithString(urlString)!!

        // Open the third-party application
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>()) { success ->
            if (!success) {
                val streetMeasureAppStoreId = TBD
                val storePageUrl = NSURL.URLWithString("itms-apps://itunes.apple.com/app/id$streetMeasureAppStoreId")!!
                UIApplication.sharedApplication.openURL(storePageUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
                MeasureResultRegistry.onResultReceived(MeasureResult.NotInstalled)
            }
        }

        // Suspend until Swift/Objective-C catches the deep link and pushes it to the registry
        return MeasureResultRegistry.incomingResults.first()

        */

        // and then, in the swift application class, the result URL needs to be parsed and sent to
        // MeasureResultRegistry
    }
}

object MeasureResultRegistry {
    private val _incomingResults = MutableSharedFlow<ArMeasureResult?>(extraBufferCapacity = 1)
    val incomingResults = _incomingResults.asSharedFlow()

    fun onResultReceived(result: ArMeasureResult) {
        _incomingResults.tryEmit(result)
    }
}
