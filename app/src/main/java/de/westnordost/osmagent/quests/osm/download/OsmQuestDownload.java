package de.westnordost.osmagent.quests.osm.download;


import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmagent.quests.QuestListener;
import de.westnordost.osmagent.quests.osm.ElementGeometry;
import de.westnordost.osmagent.quests.osm.OsmQuest;
import de.westnordost.osmagent.quests.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.quests.osm.persist.MergedElementDao;
import de.westnordost.osmagent.quests.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.quests.osm.persist.OsmElementKey;
import de.westnordost.osmagent.quests.osm.types.OverpassQuestType;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;

// TODO test case
public class OsmQuestDownload
{
	private static final String TAG = "QuestDownload";

	private final OverpassMapDataDao overpassServer;
	private final ElementGeometryDao geometryDB;
	private final MergedElementDao elementDB;
	private final OsmQuestDao osmQuestDB;
	private final Provider<List<OverpassQuestType>> questListProvider;

	private QuestListener questListener;
	private int visibleAmount;

	@Inject public OsmQuestDownload(
			OverpassMapDataDao overpassServer, ElementGeometryDao geometryDB,
			MergedElementDao elementDB, OsmQuestDao osmQuestDB,
			Provider<List<OverpassQuestType>> questListProvider)
	{
		this.overpassServer = overpassServer;
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
		this.osmQuestDB = osmQuestDB;
		this.questListProvider = questListProvider;
	}

	public void setQuestListener(QuestListener questListener)
	{
		this.questListener = questListener;
	}

	public void download(BoundingBox bbox, final Set<LatLon> blacklistedPositions,
						 Integer maxVisibleAmount, AtomicBoolean cancelState)
	{

		List<OverpassQuestType> questTypes = questListProvider.get();

		for(final OverpassQuestType questType : questTypes)
		{
			if(cancelState.get()) break;
			if(maxVisibleAmount != null && visibleAmount >= maxVisibleAmount) break;

			try
			{
				downloadQuestType(questType, bbox, blacklistedPositions);
			}
			catch(Exception e)
			{
				Log.e(TAG, "Error while downloading quest type" +
						questType.getClass().getSimpleName(), e);
			}
		}

		geometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();
	}

	private void downloadQuestType(final OverpassQuestType questType, BoundingBox bbox,
								   final Set<LatLon> blacklistedPositions)
	{
		final Map<OsmElementKey, OsmQuest> oldQuestsByElementKey = new HashMap<>();
		for(OsmQuest quest : osmQuestDB.getAll(bbox, null, questType))
		{
			oldQuestsByElementKey.put(
					new OsmElementKey(quest.getElementType(), quest.getElementId()),
					quest);
		}

		String oql = questType.getOverpassQuery(bbox);

		overpassServer.get(oql, new MapDataWithGeometryHandler()
		{
			@Override public void handle(Element element, ElementGeometry geometry)
			{
				if(!questType.appliesTo(element)) return;

				// invalid geometry -> can't show this quest, so skip it
				if(geometry == null) return;

				// do not create quests whose marker is at a blacklisted position
				if(blacklistedPositions != null && blacklistedPositions.contains(geometry.center))
				{
					return;
				}

				elementDB.put(element);

				OsmQuest quest = new OsmQuest(questType, element.getType(), element.getId(), geometry);

				// geometry must be put into DB first because quest has a foreign key on it
				geometryDB.put(quest.getElementType(), quest.getElementId(), quest.getGeometry());
				if(osmQuestDB.add(quest))
				{
					if(questListener != null)
					{
						questListener.onQuestCreated(quest);
					}
				}
				++visibleAmount;

				oldQuestsByElementKey.remove(
						new OsmElementKey(quest.getElementType(), quest.getElementId()));

			}
		});

		removeObsoleteQuests(oldQuestsByElementKey.values());
	}

	private void removeObsoleteQuests(Collection<OsmQuest> oldQuests)
	{
		if(!oldQuests.isEmpty())
		{
			for (OsmQuest quest : oldQuests)
			{
				if(osmQuestDB.delete(quest.getId()))
				{
					if(questListener != null)
					{
						questListener.onQuestRemoved(quest);
					}
				}
			}
		}
	}
}
