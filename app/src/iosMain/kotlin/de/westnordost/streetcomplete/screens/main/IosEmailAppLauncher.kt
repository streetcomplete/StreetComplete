package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIApplication

object IosEmailAppLauncher : EmailAppLauncher {
    override fun compose(email: String, subject: String?, body: String?) {
        val components = NSURLComponents().apply {
            scheme = "mailto"
            path = email
            queryItems = buildList {
                subject?.let { add(NSURLQueryItem(name = "subject", value = it)) }
                body?.let { add(NSURLQueryItem(name = "body", value = it)) }
            }
        }
        val url = components.URL ?: return
        val app = UIApplication.sharedApplication
        if (app.canOpenURL(url)) {
            app.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
        }
    }

    override fun isAvailable(): Boolean {
        val url = NSURLComponents.componentsWithString("mailto:")?.URL
        return url != null && UIApplication.sharedApplication.canOpenURL(url)
    }
}

@Composable
actual fun rememberEmailAppLauncher(): EmailAppLauncher = IosEmailAppLauncher
