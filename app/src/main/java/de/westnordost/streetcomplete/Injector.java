package de.westnordost.streetcomplete;

import de.westnordost.streetcomplete.data.DbModule;
import de.westnordost.streetcomplete.data.OsmApiModule;
import de.westnordost.streetcomplete.data.download.DownloadModule;
import de.westnordost.streetcomplete.data.meta.MetadataModule;
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesModule;
import de.westnordost.streetcomplete.data.upload.UploadModule;
import de.westnordost.streetcomplete.data.upload.UploadModule2;
import de.westnordost.streetcomplete.data.user.UserModule;
import de.westnordost.streetcomplete.data.user.achievements.AchievementsModule;
import de.westnordost.streetcomplete.quests.QuestModule;
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsModule;

public enum Injector
{
	instance;

	private ApplicationComponent applicationComponent;

	void initializeApplicationComponent(StreetCompleteApplication app)
	{
		applicationComponent = DaggerApplicationComponent.builder()
				.applicationModule(new ApplicationModule(app))
				// not sure why it is necessary to add these all by hand, I must be doing something wrong
				.achievementsModule(AchievementsModule.INSTANCE)
				.dbModule(DbModule.INSTANCE)
				.downloadModule(DownloadModule.INSTANCE)
				.metadataModule(MetadataModule.INSTANCE)
				.osmApiModule(OsmApiModule.INSTANCE)
				.osmNotesModule(OsmNotesModule.INSTANCE)
				.questModule(QuestModule.INSTANCE)
				.trafficFlowSegmentsModule(TrafficFlowSegmentsModule.INSTANCE)
				.uploadModule(UploadModule.INSTANCE)
				.userModule(UserModule.INSTANCE)
				.build();
	}

	public ApplicationComponent getApplicationComponent() {
		return applicationComponent;
	}
}
