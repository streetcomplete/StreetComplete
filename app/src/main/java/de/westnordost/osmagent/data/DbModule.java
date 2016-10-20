package de.westnordost.osmagent.data;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.OsmagentConstants;
import de.westnordost.osmagent.data.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.data.statistics.QuestStatisticsDao;
import de.westnordost.osmagent.util.KryoSerializer;
import de.westnordost.osmagent.util.Serializer;
import de.westnordost.osmapi.changesets.ChangesetsDao;

@Module
public class DbModule
{
	@Provides @Singleton public static SQLiteOpenHelper sqliteOpenHelper(Context ctx)
	{
		return new OsmagentOpenHelper(ctx);
	}

	@Provides @Singleton public static Serializer serializer()
	{
		return new KryoSerializer();
	}

	@Provides @Singleton public static QuestStatisticsDao questStatisticsDao(
			SQLiteOpenHelper dbHelper, ChangesetsDao changesetsDao)
	{
		return new QuestStatisticsDao(dbHelper, changesetsDao);
	}

	@Provides @Singleton public static OsmQuestDao osmQuestDao(
			SQLiteOpenHelper dbHelper, Serializer serializer)
	{
		return new OsmQuestDao(dbHelper, serializer, OsmagentConstants.OSM_QUESTS_PACKAGE);
	}

}
