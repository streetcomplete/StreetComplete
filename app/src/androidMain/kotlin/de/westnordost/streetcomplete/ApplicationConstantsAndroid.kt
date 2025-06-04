package de.westnordost.streetcomplete

object ApplicationConstantsAndroid {

    const val USER_AGENT = ApplicationConstants.NAME + " " + BuildConfig.VERSION_NAME

    // name is "downloading" for historic reasons, not sure if it has any side-effects if it is changed now
    const val NOTIFICATIONS_CHANNEL_SYNC = "downloading"
    const val NOTIFICATIONS_ID_SYNC = 1

    const val STREETMEASURE = "de.westnordost.streetmeasure"
}
