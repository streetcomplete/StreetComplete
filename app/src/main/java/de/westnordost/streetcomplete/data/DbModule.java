package de.westnordost.streetcomplete.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList;
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao;
import de.westnordost.streetcomplete.util.KryoSerializer;
import de.westnordost.streetcomplete.util.Serializer;

@Module
public class DbModule
{
	@Provides @Singleton public static SQLiteOpenHelper sqliteOpenHelper(Context ctx)
	{
		return sqliteOpenHelper(ctx, ApplicationConstants.DATABASE_NAME);
	}

	public static SQLiteOpenHelper sqliteOpenHelper(Context ctx, String databaseName)
	{
		return new StreetCompleteSQLiteOpenHelper(ctx, databaseName);
	}

	@Provides @Singleton public static Serializer serializer()
	{
		return new KryoSerializer();
	}

	@Provides @Singleton public static VisibleQuestTypeDao visibleQuestTypeDao(
			SQLiteOpenHelper dbHelper)
	{
		return new VisibleQuestTypeDao(dbHelper);
	}

	@Provides @Singleton public static QuestTypeOrderList questTypeOrderDao(
			SharedPreferences prefs, QuestTypeRegistry questTypeRegistry)
	{
		return new QuestTypeOrderList(prefs, questTypeRegistry);
	}
}
