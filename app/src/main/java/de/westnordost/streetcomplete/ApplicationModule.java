package de.westnordost.streetcomplete;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.List;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.QuestController;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.download.MobileDataAutoDownloadStrategy;
import de.westnordost.streetcomplete.data.download.WifiAutoDownloadStrategy;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment;
import de.westnordost.streetcomplete.sound.SoundFx;
import de.westnordost.streetcomplete.tools.CrashReportExceptionHandler;

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

	@Provides public AssetManager assetManager()
	{
		return application.getAssets();
	}

	@Provides public Resources resources()
	{
		return application.getResources();
	}

	@Provides public QuestController questController(
			OsmQuestDao osmQuestDB, UndoOsmQuestDao undoOsmQuestDB, MergedElementDao osmElementDB,
			ElementGeometryDao geometryDB, OsmNoteQuestDao osmNoteQuestDB,
			CreateNoteDao createNoteDB, OpenChangesetsDao manageChangesetsDB,
			Provider<List<QuestType>> questTypesProvider)
	{
		return new QuestController(
				osmQuestDB, undoOsmQuestDB, osmElementDB, geometryDB, osmNoteQuestDB, createNoteDB,
				manageChangesetsDB,	questTypesProvider, appContext());
	}

	@Provides public static MobileDataAutoDownloadStrategy mobileDataAutoDownloadStrategy(
			OsmQuestDao osmQuestDB, DownloadedTilesDao downloadedTilesDao,
			Provider<List<QuestType>> questTypesProvider)
	{
		return new MobileDataAutoDownloadStrategy(osmQuestDB, downloadedTilesDao, questTypesProvider);
	}

	@Provides public static WifiAutoDownloadStrategy wifiAutoDownloadStrategy(
			OsmQuestDao osmQuestDB, DownloadedTilesDao downloadedTilesDao,
			Provider<List<QuestType>> questTypesProvider)
	{
		return new WifiAutoDownloadStrategy(osmQuestDB, downloadedTilesDao, questTypesProvider);
	}

	@Provides public static LocationRequestFragment locationRequestComponent()
	{
		return new LocationRequestFragment();
	}

	@Provides @Singleton public static CrashReportExceptionHandler serializer(Context ctx)
	{
		return new CrashReportExceptionHandler(ctx);
	}

	@Provides public static OsmOAuthDialogFragment osmOAuthFragment()
	{
		return new OsmOAuthDialogFragment();
	}

	@Provides public SoundFx soundFx()
	{
		return new SoundFx(appContext());
	}
}
