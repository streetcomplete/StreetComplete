package de.westnordost.streetcomplete.screens

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.util.getSelectedLocale
import de.westnordost.streetcomplete.util.getSystemLocales
import de.westnordost.streetcomplete.util.ktx.addedToFront
import de.westnordost.streetcomplete.util.setDefaultLocales
import de.westnordost.streetcomplete.util.setLocales
import org.koin.android.ext.android.inject
import java.util.Locale

open class BaseActivity : AppCompatActivity {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    private val prefs: ObservableSettings by inject()

    private var locale: Locale? = null

    override fun attachBaseContext(base: Context) {
        val locale = getSelectedLocale(prefs)
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
        if (locale != getSelectedLocale(prefs)) {
            ActivityCompat.recreate(this)
        }
    }
}
