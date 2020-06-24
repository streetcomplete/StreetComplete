package de.westnordost.streetcomplete.data.osmnotes

import android.content.Context
import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.ApplicationConstants
import java.io.File
import javax.inject.Named

@Module
object OsmNotesModule {

    /* NOTE: most dependents don't actually let dagger inject this dependency but just use this
	   static method to initialize it themselves. This is not clean, but for some reason, having an
	   @Inject @Named("AvatarsCacheDirectory") internal lateinit var avatarsCacheDirectory: File
	   doesn't work. Dagger2 always reports that it does not know how to inject it.
	*/
    @Provides @Named("AvatarsCacheDirectory")
    fun getAvatarsCacheDirectory(context: Context): File {
        return File(context.cacheDir, ApplicationConstants.AVATARS_CACHE_DIRECTORY)
    }

    @Provides
    fun imageUploader(): StreetCompleteImageUploader =
        StreetCompleteImageUploader(ApplicationConstants.SC_PHOTO_SERVICE_URL)
}