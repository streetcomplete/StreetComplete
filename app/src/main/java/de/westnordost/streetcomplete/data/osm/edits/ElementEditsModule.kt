package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditUploader
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditsUploader
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloser
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloserWorker
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val elementEditsModule = module {
    factory { ChangesetAutoCloser(get()) }
    factory { ElementEditUploader(get(), get(), get()) }

    factory { ElementEditsDao(get(), get(), get()) }
    factory { ElementIdProviderDao(get()) }
    factory { LastEditTimeStore(get()) }
    factory { OpenChangesetsDao(get()) }

    single { OpenChangesetsManager(get(), get(), get(), get()) }

    single { ElementEditsUploader(get(), get(), get(), get(), get(), get()) }

    single<ElementEditsSource> { get<ElementEditsController>() }
    single { ElementEditsController(get(), get(), get()) }
    single { MapDataWithEditsSource(get(), get(), get()) }

    worker { ChangesetAutoCloserWorker(get(), get(), get()) }
}
