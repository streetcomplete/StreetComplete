package de.westnordost.osmagent.quests;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.util.KryoSerializer;
import de.westnordost.osmagent.util.Serializer;

@Module
public class DbModule
{
	@Provides @Singleton public static SQLiteOpenHelper provideSQLiteOpenHelper(Context ctx)
	{
		return new OsmagentOpenHelper(ctx);
	}

	@Provides @Singleton public static Serializer provideSerializer()
	{
		return new KryoSerializer();
	}
}
