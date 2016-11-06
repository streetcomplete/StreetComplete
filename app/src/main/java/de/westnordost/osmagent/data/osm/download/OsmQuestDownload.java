package de.westnordost.osmagent.data.osm.download;


import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.data.QuestGroup;
import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.data.QuestTypes;
import de.westnordost.osmagent.data.VisibleQuestListener;
import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osm.OverpassQuestType;
import de.westnordost.osmagent.data.osm.persist.ElementGeometryDao;
import de.westnordost.osmagent.data.osm.persist.MergedElementDao;
import de.westnordost.osmagent.data.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.data.osm.persist.OsmElementKey;
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
	private final QuestTypes questTypeList;

	private VisibleQuestListener questListener;
	private int visibleAmount;

	@Inject public OsmQuestDownload(
			OverpassMapDataDao overpassServer, ElementGeometryDao geometryDB,
			MergedElementDao elementDB, OsmQuestDao osmQuestDB,
			QuestTypes questTypeList)
	{
		this.overpassServer = overpassServer;
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
		this.osmQuestDB = osmQuestDB;
		this.questTypeList = questTypeList;
	}

	public void setQuestListener(VisibleQuestListener questListener)
	{
		this.questListener = questListener;
	}

	public void download(BoundingBox bbox, final Set<LatLon> blacklistedPositions,
						 Integer maxVisibleAmount, AtomicBoolean cancelState)
	{

		List<QuestType> questTypes = questTypeList.getQuestTypesSortedByImportance();

		for(QuestType questType : questTypes)
		{
			if(!(questType instanceof OverpassQuestType)) continue;
			if(cancelState.get()) break;
			if(maxVisibleAmount != null && visibleAmount >= maxVisibleAmount) break;

			try
			{
				downloadQuestType((OverpassQuestType) questType, bbox, blacklistedPositions);
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
		for(OsmQuest quest : osmQuestDB.getAll(bbox, null, questType, null, null))
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
						questListener.onQuestCreated(quest, QuestGroup.OSM);
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
						questListener.onQuestRemoved(quest, QuestGroup.OSM);
					}
				}
			}
		}
	}
}
