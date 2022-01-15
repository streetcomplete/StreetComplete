package de.westnordost.streetcomplete.data.upload

import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.ApplicationConstants
import kotlinx.coroutines.sync.Mutex
import javax.inject.Named
import javax.inject.Singleton

@Module
object UploadModule {
    @Provides fun checkVersionIsBanned(): VersionIsBannedChecker =
        VersionIsBannedChecker(
            "https://www.westnordost.de/streetcomplete/banned_versions.txt",
            ApplicationConstants.USER_AGENT
        )

    @Provides fun uploadProgressSource(uploadController: UploadController): UploadProgressSource =
        uploadController

    /** uploading and downloading should be serialized, i.e. may not run in parallel, to avoid
     *  certain race-condition.
     *
     *  Example:
     *  A download of refreshed OSM data takes 10 seconds. While the download is executing, the user
     *  solves a quest (based on the previously downloaded data) which is immediately uploaded,
     *  resulting in the updated element to be persisted.
     *  When the download finally finishes, it got the data from 10 seconds ago, before the element
     *  has been updated. Thus, the old element overwrites the new one. */
    @Provides @Singleton @Named("SerializeSync") fun serializedSync(): Mutex = Mutex()
}
