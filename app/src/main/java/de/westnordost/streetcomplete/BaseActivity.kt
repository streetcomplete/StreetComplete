package de.westnordost.streetcomplete

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import java.util.*

open class BaseActivity : AppCompatActivity {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    private var locale: Locale? = null

    override fun attachBaseContext(base: Context) {
        locale = getSelectedLocale(base)

        val newBase = if (locale != null) {
            base.createConfigurationContext(Configuration().also { it.setLocale(locale) })
        } else base

        super.attachBaseContext(newBase)
    }

    override fun onRestart() {
        super.onRestart()
        // force restart if the locale changed while the activity was in background
        if (locale != getSelectedLocale(this)) {
            ActivityCompat.recreate(this)
        }
    }
}

private fun getSelectedLocale(context: Context): Locale? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val languageTag = prefs.getString(Prefs.LANGUAGE_SELECT, "") ?: ""
    return if (languageTag.isEmpty()) null else Locale.forLanguageTag(languageTag)
}
