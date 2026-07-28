package de.westnordost.streetcomplete.screens.main

import androidx.compose.ui.util.fastMap
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

object IosEmailAppLauncher : EmailAppLauncher {
    override fun compose(email: String, subject: String?, body: String?) {
        val encodedSubject = (subject as NSString?)
            ?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet)

        val encodedBody = (body as NSString?)
            ?.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet)

        val urlString = buildString {
            append("mailto:$email")
            if (encodedSubject != null) {
                append("?")
                append("subject=$encodedSubject")
            }
            if (encodedBody != null) {
                if (encodedSubject != null) append("&") else append("?")
                append("body=$encodedBody")
            }
        }

        val url = NSURL.URLWithString(urlString)

        if (url != null) {
            val app = UIApplication.sharedApplication
            if (app.canOpenURL(url)) {
                app.openURL(url)
            }
        }
    }

    override fun isAvailable(): Boolean {
        val url = NSURL.URLWithString("mailto:")
        return url != null && UIApplication.sharedApplication.canOpenURL(url)
    }
}
