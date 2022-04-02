package de.westnordost.streetcomplete.screens

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import de.westnordost.streetcomplete.util.getSelectedLocale
import de.westnordost.streetcomplete.util.getSystemLocales
import de.westnordost.streetcomplete.util.ktx.addedToFront
import de.westnordost.streetcomplete.util.setDefaultLocales
import de.westnordost.streetcomplete.util.setLocales
import java.util.Locale

open class BaseActivity : AppCompatActivity {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    private var locale: Locale? = null

    override fun attachBaseContext(base: Context) {
        val locale = getSelectedLocale(base)
        this.locale = locale

        var newBase = base

        if (locale != null) {
            val locales = getSystemLocales().addedToFront(locale)
            setDefaultLocales(locales)
            newBase = base.createConfigurationContext(Configuration().also { it.setLocales(locales) })
        }

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
