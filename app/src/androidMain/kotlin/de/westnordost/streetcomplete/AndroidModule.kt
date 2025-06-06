package de.westnordost.streetcomplete

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import de.westnordost.streetcomplete.data.AndroidDatabase
import de.westnordost.streetcomplete.data.CleanerWorker
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.StreetCompleteSQLiteOpenHelper
import de.westnordost.streetcomplete.data.connection.InternetConnectionState
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadControllerAndroid
import de.westnordost.streetcomplete.data.download.DownloadWorker
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloaderAndroid
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloser
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloserAndroid
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloserWorker
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadControllerAndroid
import de.westnordost.streetcomplete.data.upload.UploadWorker
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway
import de.westnordost.streetcomplete.quests.existence.CheckExistence
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.shop_type.CheckShopExistence
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

val androidModule = module {
    // Database on Android

    single<Database> {
        val sqLite = StreetCompleteSQLiteOpenHelper(get(), ApplicationConstants.DATABASE_NAME)
        AndroidDatabase(sqLite.writableDatabase)
    }

    // Workmanager-based on Android

    single<UploadController> { UploadControllerAndroid(androidContext()) }
    worker { UploadWorker(get(), androidContext(), get()) }

    single<DownloadController> { DownloadControllerAndroid(androidContext()) }
    worker { DownloadWorker(get(), androidContext(), get()) }

    factory<ChangesetAutoCloser> { ChangesetAutoCloserAndroid(androidContext()) }
    worker { ChangesetAutoCloserWorker(get(), androidContext(), get()) }

    worker { CleanerWorker(get(), get(), get()) }

    factory<MapTilesDownloader> { MapTilesDownloaderAndroid(androidContext()) }

    factory<InternetConnectionState> { InternetConnectionState(androidContext()) }

    // Settings

    single<ObservableSettings> { SharedPreferencesSettings.Factory(androidContext()).create() }

    single { OsmQuestController(get(), get(), get(), get(), get(named("CountryBoundariesLazy")), ::getAnalyzePriority) }
}

// TODO multiplatform: can be moved once all the quest types are in common
/** an index by which a list of quest types can be sorted so that quests that are the slowest to
 *  evaluate are evaluated first. This is a performance improvement because the evaluation is done
 *  in parallel on as many threads as there are CPU cores. So if all threads are done except one,
 *  all have to wait for that one thread. So, better enqueue the expensive work at the beginning. */
private fun getAnalyzePriority(questType: OsmElementQuestType<*>): Int = when (questType) {
    is AddOpeningHours -> 0 // OpeningHoursParser, extensive filter
    is CheckExistence -> 1 // FeatureDictionary, extensive filter
    is CheckShopExistence -> 1 // FeatureDictionary, extensive filter
    is AddHousenumber -> 1 // complex filter
    is AddMaxHeight -> 1 // complex filter
    is AddCycleway -> 2 // complex filter
    is AddPlaceName -> 2 // FeatureDictionary, extensive filter
    else -> 10
}
