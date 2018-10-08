package de.westnordost.streetcomplete.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao;
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList;
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTablesHelper;
import de.westnordost.streetcomplete.quests.oneway.WayTrafficFlowTablesHelper;
import de.westnordost.streetcomplete.util.KryoSerializer;
import de.westnordost.streetcomplete.util.Serializer;
import de.westnordost.osmapi.changesets.ChangesetsDao;

@Module
public class DbModule
{
	@Provides @Singleton public static SQLiteOpenHelper sqliteOpenHelper(Context ctx)
	{
		return new StreetCompleteOpenHelper(ctx, new TablesHelper[]{
			new RoadNamesTablesHelper(), new WayTrafficFlowTablesHelper()
		});
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

	@Provides @Singleton public static OpenChangesetsDao changesetsManagerDao(
			SQLiteOpenHelper dbHelper, SharedPreferences prefs)
	{
		return new OpenChangesetsDao(dbHelper,prefs);
	}

	@Provides @Singleton public static OsmQuestDao osmQuestDao(
			SQLiteOpenHelper dbHelper, Serializer serializer, QuestTypeRegistry questTypeList)
	{
		return new OsmQuestDao(dbHelper, serializer, questTypeList);
	}

	@Provides @Singleton public static UndoOsmQuestDao undoOsmQuestDao(
			SQLiteOpenHelper dbHelper, Serializer serializer, QuestTypeRegistry questTypeList)
	{
		return new UndoOsmQuestDao(dbHelper, serializer, questTypeList);
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

	@Provides public static List<QuestType> visibleQuestTypes(
			QuestTypeRegistry questTypeRegistry, VisibleQuestTypeDao visibleQuestTypeDao,
			QuestTypeOrderList questTypeOrderList)
	{
		List<QuestType> questTypes = new ArrayList<>(questTypeRegistry.getAll());
		Iterator<QuestType> it = questTypes.listIterator();
		while(it.hasNext())
		{
			QuestType questType = it.next();
			if(!visibleQuestTypeDao.isVisible(questType))
			{
				it.remove();
			}
		}
		questTypeOrderList.sort(questTypes);

		return questTypes;
	}
}
