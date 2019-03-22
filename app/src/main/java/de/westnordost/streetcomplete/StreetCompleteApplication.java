package de.westnordost.streetcomplete;

import android.app.Application;
import android.content.SharedPreferences;

import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatDelegate;
import de.westnordost.countryboundaries.CountryBoundaries;
import de.westnordost.streetcomplete.tangram.TangramQuestSpriteSheetCreator;

public class StreetCompleteApplication extends Application
{
	@Inject FutureTask<CountryBoundaries> countryBoundariesFuture;
	@Inject TangramQuestSpriteSheetCreator spriteSheetCreator;
	@Inject SharedPreferences prefs;

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
		// sprite sheet is necessary to display quests
		new Thread(() -> spriteSheetCreator.get()).start();
		// country boundaries are necessary latest for when a quest is opened
		new Thread(countryBoundariesFuture).start();
	}
}
