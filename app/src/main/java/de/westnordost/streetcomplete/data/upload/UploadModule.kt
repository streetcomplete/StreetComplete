package de.westnordost.streetcomplete.data.upload

import dagger.Module
import dagger.Provides
import de.westnordost.streetcomplete.ApplicationConstants

@Module
object UploadModule {
    @Provides fun checkVersionIsBanned(): VersionIsBannedChecker =
        VersionIsBannedChecker(
            "https://www.westnordost.de/streetcomplete/banned_versions.txt",
            ApplicationConstants.USER_AGENT
        )

    @Provides fun uploadProgressSource(uploadController: UploadController): UploadProgressSource =
        uploadController
}