package de.westnordost.streetcomplete

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.location.LocationRequestFragment
import de.westnordost.streetcomplete.util.CrashReportExceptionHandler
import de.westnordost.streetcomplete.util.SoundFx
import javax.inject.Singleton


@Module class ApplicationModule(private val application: Application) {

    @Provides fun appContext(): Context = application

    @Provides fun application(): Application = application

    @Provides fun preferences(): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)

    @Provides fun assetManager(): AssetManager = application.assets

    @Provides fun resources(): Resources = application.resources

    @Provides fun locationRequestComponent(): LocationRequestFragment = LocationRequestFragment()

    @Provides @Singleton fun soundFx(): SoundFx = SoundFx(appContext())

    @Provides @Singleton fun exceptionHandler(ctx: Context): CrashReportExceptionHandler =
        CrashReportExceptionHandler(
            ctx,
            "streetcomplete_errors@westnordost.de",
            "crashreport.txt"
        )
}
