package de.westnordost.streetcomplete.util.error_reporting

interface CrashReportHolder {
    /** Gets the last crash report, if any, that was generated last time the app crashed through
     *  an uncaught exception. The crash report is then deleted. */
    fun takeCrashReport(): String?
}
