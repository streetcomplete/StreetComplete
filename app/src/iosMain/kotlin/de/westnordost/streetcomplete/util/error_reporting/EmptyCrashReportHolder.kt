package de.westnordost.streetcomplete.util.error_reporting

object EmptyCrashReportHolder : CrashReportHolder {
    /* Anonymized crashes in iOS apps are automatically posted to App Store Connect (the developer
       web dashboard), so it is not necessary to manually register an uncaught exception handler
       and save these reports as files, to be sent via email to the developer on the next app
       start.

       For apps installed via Google Play, it is also not necessary on Android because crashes will
       be posted in the Google Play Console (the developer web dashboard). We use this method only
       for apps not installed via Google Play (e.g. F-Droid, or direct APK install), see
       [CrashReportsUncaughtExceptionHandler]. */
    override fun takeCrashReport(): String? = null
}
