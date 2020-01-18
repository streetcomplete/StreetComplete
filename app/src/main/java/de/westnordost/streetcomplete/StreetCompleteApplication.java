package de.westnordost.streetcomplete;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatDelegate;
import de.westnordost.countryboundaries.CountryBoundaries;
import de.westnordost.osmfeatures.FeatureDictionary;

public class StreetCompleteApplication extends Application
{
	@Inject FutureTask<CountryBoundaries> countryBoundariesFuture;
	@Inject FutureTask<FeatureDictionary> featuresDictionaryFuture;
	@Inject SharedPreferences prefs;

	private static final String PRELOAD_TAG = "Preload";

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this);

		Injector.instance.initializeApplicationComponent(this);
		Injector.instance.getApplicationComponent().inject(this);
		preload();

		Prefs.Theme theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "LIGHT"));
		AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode);
	}

	/** Load some things in the background that are needed later */
	private void preload()
	{
		Log.i(PRELOAD_TAG, "Preloading data");

		// country boundaries are necessary latest for when a quest is opened
		new Thread(() -> {
			countryBoundariesFuture.run();
			Log.i(PRELOAD_TAG, "Loaded country boundaries");
		}).start();

		// names dictionary is necessary when displaying an element that has no name or
		// when downloading the place name quest
		new Thread(() -> {
			featuresDictionaryFuture.run();
			Log.i(PRELOAD_TAG, "Loaded features dictionary");
		}).start();
	}
}
