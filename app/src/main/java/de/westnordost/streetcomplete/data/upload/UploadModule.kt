package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.ApplicationConstants
import kotlinx.coroutines.sync.Mutex
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

val uploadModule = module {
    factory { VersionIsBannedChecker("https://www.westnordost.de/streetcomplete/banned_versions.txt", ApplicationConstants.USER_AGENT) }

    single { Uploader(get(), get(), get(), get(), get(), get(named("SerializeSync"))) }
    /** uploading and downloading should be serialized, i.e. may not run in parallel, to avoid
     *  certain race-condition.
     *
     *  Example:
     *  A download of refreshed OSM data takes 10 seconds. While the download is executing, the user
     *  solves a quest (based on the previously downloaded data) which is immediately uploaded,
     *  resulting in the updated element to be persisted.
     *  When the download finally finishes, it got the data from 10 seconds ago, before the element
     *  has been updated. Thus, the old element overwrites the new one. */
    single(named("SerializeSync")) { Mutex() }

    single<UploadProgressSource> { get<Uploader>() }
    single { UploadController(get()) }

    worker { UploadWorker(get(), androidContext(), get()) }
}
