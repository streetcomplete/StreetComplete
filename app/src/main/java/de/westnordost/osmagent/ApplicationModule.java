package de.westnordost.osmagent;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.data.meta.CurrentCountry;
import de.westnordost.osmagent.data.osm.download.OverpassQuestTypeList;
import de.westnordost.osmagent.data.osm.OverpassQuestType;

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

	@Provides public static List<OverpassQuestType> questTypeListProvider()
	{
		return OverpassQuestTypeList.quests;
	}

	@Provides public static CurrentCountry localeMetadata(Context appContext)
	{
		return new CurrentCountry(appContext);
	}
}
