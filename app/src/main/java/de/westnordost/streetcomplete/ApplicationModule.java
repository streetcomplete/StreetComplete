package de.westnordost.streetcomplete;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
