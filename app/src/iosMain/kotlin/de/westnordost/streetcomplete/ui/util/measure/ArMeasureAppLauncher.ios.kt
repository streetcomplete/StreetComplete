package de.westnordost.streetcomplete.ui.util.measure

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.LengthUnit

@Composable
actual fun rememberArMeasureAppLauncher(): ArMeasureAppLauncher {
    return object : ArMeasureAppLauncher {
        override fun measure(
            lengthUnit: LengthUnit,
            measureVertical: Boolean,
            onResult: (ArMeasureResult) -> Unit,
        ) {
            // As StreetMeasure doesn't exist on iOS, no point in trying to launch it there.
            onResult(ArMeasureResult.Error)

            // However, if  it one day exists, we can try the following approach
            // (warning, needs testing):

            /*

            ArMeasureAppResultManager.setCallback(onResult)

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

                    ExternalAppResultManager.onResultReceived(MeasureResult.NotInstalled)
                }
            }

            // TBD: and then somehow get the result from the app
            // in the swift application class, the result deep link URL needs to be parsed and sent
            // to ArMeasureAppResultManager.onResultReceived(...)

             */
        }
    }
}

/*
object ArMeasureAppResultManager {
    private var pendingCallback: ((ArMeasureResult) -> Unit)? = null

    fun setCallback(callback: (ArMeasureResult) -> Unit) {
        pendingCallback = callback
    }

    fun onResultReceived(result: ArMeasureResult) {
        pendingCallback?.invoke(result)
        pendingCallback = null
    }
}
*/
