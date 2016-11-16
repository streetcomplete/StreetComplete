package de.westnordost.osmagent;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.data.QuestController;
import de.westnordost.osmagent.data.QuestDownloadService;
import de.westnordost.osmagent.data.meta.CurrentCountry;
import de.westnordost.osmagent.data.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.data.osm.persist.MergedElementDao;
import de.westnordost.osmagent.data.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.data.osmnotes.CreateNoteDao;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuestDao;

@Module
public class ApplicationModule
{
	private final Application application;

	public ApplicationModule(Application application)
	{
		this.application = application;
	}

	@Provides public Context appContext()
	{
		return application;
	}

	@Provides public Application application()
	{
		return application;
	}

	@Provides public SharedPreferences preferences()
	{
		return PreferenceManager.getDefaultSharedPreferences(application);
	}

	@Provides public Resources resources()
	{
		return application.getResources();
	}

	@Provides public static CurrentCountry localeMetadata(Context appContext)
	{
		return new CurrentCountry(appContext);
	}

	@Provides public QuestController questController(
			OsmQuestDao osmQuestDB, MergedElementDao osmElementDB, ElementGeometryDao geometryDB,
			OsmNoteQuestDao osmNoteQuestDB,	CreateNoteDao createNoteDB)
	{
		return new QuestController(
				osmQuestDB, osmElementDB, geometryDB, osmNoteQuestDB, createNoteDB, appContext());
	}
}
