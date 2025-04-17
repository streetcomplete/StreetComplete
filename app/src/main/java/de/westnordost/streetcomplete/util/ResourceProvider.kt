package de.westnordost.streetcomplete.util

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import java.util.Locale

/**
 * Provides access to Android resources, like strings.
 *
 * Useful for when one needs to access resources in classes where a [Context] is not directly
 * available.
 */
interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
}

class DefaultResourceProvider(
    private val context: Context,
    private val preferences: Preferences,
) : ResourceProvider {

    @Volatile
    private var cachedLocale: Locale? = null

    @Volatile
    private var cachedLocaleContext: Context? = null

    private val lock = Any()

    private var languageListener: SettingsListener? = null

    init {
        languageListener = preferences.onLanguageChanged { newLanguageCode ->
            synchronized(lock) {
                cachedLocale = null
                cachedLocaleContext = null
            }
        }
    }

    override fun getString(resId: Int): String {
        val currentAppLocale: Locale? = getSelectedLocale(preferences)

        var localContext = cachedLocaleContext
        var localLocale = cachedLocale

        if (localContext == null || localLocale != currentAppLocale) {
            synchronized(lock) {
                localLocale = cachedLocale
                if (cachedLocaleContext == null || localLocale != currentAppLocale) {
                    val overrideConfiguration = Configuration(context.resources.configuration)
                    overrideConfiguration.setLocale(currentAppLocale)
                    localContext = context.createConfigurationContext(overrideConfiguration)

                    cachedLocaleContext = localContext
                    cachedLocale = currentAppLocale
                } else {
                    localContext = cachedLocaleContext
                }
            }
        }

        return localContext!!.getString(resId)
    }
}
