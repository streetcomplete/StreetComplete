package de.westnordost.osmagent.data.meta;

import java.io.InputStream;

import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.R;

@Module
public class MetadataModule
{
	@Provides @Singleton public static Abbreviations abbreviations(CurrentCountry currentCountry)
	{
		InputStream is = currentCountry.getResources().openRawResource(R.raw.abbreviations);
		return new Abbreviations(is, currentCountry.getLocale());
	}
}
