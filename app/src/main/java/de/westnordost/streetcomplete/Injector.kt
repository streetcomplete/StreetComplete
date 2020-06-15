package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.DbModule
import de.westnordost.streetcomplete.data.OsmApiModule
import de.westnordost.streetcomplete.data.download.DownloadModule
import de.westnordost.streetcomplete.data.meta.MetadataModule
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesModule
import de.westnordost.streetcomplete.data.upload.UploadModule
import de.westnordost.streetcomplete.data.user.UserModule
import de.westnordost.streetcomplete.data.user.achievements.AchievementsModule
import de.westnordost.streetcomplete.map.MapModule
import de.westnordost.streetcomplete.quests.QuestModule
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsModule

object Injector {

    lateinit var applicationComponent: ApplicationComponent
        private set

    fun initializeApplicationComponent(app: StreetCompleteApplication?) {
        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(app!!)) // not sure why it is necessary to add these all by hand, I must be doing something wrong
            .achievementsModule(AchievementsModule)
            .dbModule(DbModule)
            .downloadModule(DownloadModule)
            .metadataModule(MetadataModule)
            .osmApiModule(OsmApiModule)
            .osmNotesModule(OsmNotesModule)
            .questModule(QuestModule)
            .trafficFlowSegmentsModule(TrafficFlowSegmentsModule)
            .uploadModule(UploadModule)
            .userModule(UserModule)
            .mapModule(MapModule)
            .build()
    }
}