package de.westnordost.osmagent.data.osm.download;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.data.QuestTypes;
import de.westnordost.osmagent.data.osm.ElementGeometry;
import de.westnordost.osmagent.data.osm.OsmQuest;
import de.westnordost.osmagent.data.osm.OverpassQuestType;
import de.westnordost.osmagent.data.osm.VisibleOsmQuestListener;
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

	// injections
	private final OverpassMapDataDao overpassServer;
	private final ElementGeometryDao geometryDB;
	private final MergedElementDao elementDB;
	private final OsmQuestDao osmQuestDB;

	// listener
	private VisibleOsmQuestListener questListener;

	@Inject public OsmQuestDownload(
			OverpassMapDataDao overpassServer, ElementGeometryDao geometryDB,
			MergedElementDao elementDB, OsmQuestDao osmQuestDB,
			QuestTypes questTypeList)
	{
		this.overpassServer = overpassServer;
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
		this.osmQuestDB = osmQuestDB;
	}

	public void setQuestListener(VisibleOsmQuestListener listener)
	{
		this.questListener = listener;
	}

	public int download(final OverpassQuestType questType, BoundingBox bbox,
						  final Set<LatLon> blacklistedPositions)
	{
		final ArrayList<ElementGeometryDao.Row> geometryRows = new ArrayList<>();
		final Map<OsmElementKey,Element> elements = new HashMap<>();
		final ArrayList<OsmQuest> quests = new ArrayList<>();
		final Map<OsmElementKey, Long> previousQuests = getPreviousQuestsIdsByElementKey(questType, bbox);

		String oql = questType.getOverpassQuery(bbox);
		overpassServer.get(oql, new MapDataWithGeometryHandler()
		{
			@Override public void handle(Element element, ElementGeometry geometry)
			{
				if(!mayCreateQuestFrom(questType, element, geometry, blacklistedPositions)) return;

				Element.Type elementType = element.getType();
				long elementId = element.getId();

				OsmElementKey elementKey = new OsmElementKey(elementType, elementId);

				geometryRows.add(new ElementGeometryDao.Row(elementType, elementId, geometry));
				elements.put(elementKey, element);
				quests.add(new OsmQuest(questType, elementType, elementId, geometry));

				previousQuests.remove(elementKey);
			}
		});

		// geometry and elements must be put into DB first because quests have foreign keys on it
		geometryDB.putAll(geometryRows);
		elementDB.putAll(elements.values());

		int newQuestsByQuestType = osmQuestDB.addAll(quests);

		if(questListener != null)
		{
			for (OsmQuest quest : quests)
			{
				// it is null if this quest is already in the DB, so don't call onQuestCreated
				if(quest.getId() == null) continue;

				OsmElementKey k = new OsmElementKey(quest.getElementType(), quest.getElementId());
				questListener.onQuestCreated(quest, elements.get(k));
			}
		}

		if(!previousQuests.isEmpty())
		{
			if(questListener != null)
			{
				for (Long questId : previousQuests.values())
				{
					questListener.onOsmQuestRemoved(questId);
				}
			}

			osmQuestDB.deleteAll(previousQuests.values());
		}

		// note: this could be done after ALL osm quest types have been downloaded if this
		// turns out to be slow if done for every quest type
		geometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();

		int visibleQuestsByQuestType = quests.size();
		int obsoleteAmount = previousQuests.size();
		Log.i(TAG, getQuestTypeAsLogString(questType) + ": " +
				"Added " + newQuestsByQuestType + " new and " +
				"removed " + obsoleteAmount + " already resolved quests." +
				" (Total: " + visibleQuestsByQuestType + ")");

		return visibleQuestsByQuestType;
	}

	private Map<OsmElementKey, Long> getPreviousQuestsIdsByElementKey(
			OverpassQuestType questType, BoundingBox bbox)
	{
		String questTypeName = questType.getClass().getSimpleName();
		Map<OsmElementKey, Long> result = new HashMap<>();
		for(OsmQuest quest : osmQuestDB.getAll(bbox, null, questTypeName, null, null))
		{
			result.put(new OsmElementKey(quest.getElementType(), quest.getElementId()),	quest.getId());
		}
		return result;
	}

	private boolean mayCreateQuestFrom(OverpassQuestType questType, Element element,
									   ElementGeometry geometry, Set<LatLon> blacklistedPositions)
	{
		if(!questType.appliesTo(element)) return false;

		// invalid geometry -> can't show this quest, so skip it
		if(geometry == null)
		{
			// classified as warning because it might very well be a bug on the geometry
			// creation on our side
			Log.w(TAG, getQuestTypeAsLogString(questType) + ": Not adding a quest " +
					" because the element " + getElementAsLogString(element) +
					" has no valid geometry");
			return false;
		}

		// do not create quests whose marker is at a blacklisted position
		if(blacklistedPositions != null && blacklistedPositions.contains(geometry.center))
		{
			Log.v(TAG, getQuestTypeAsLogString(questType) + ": Not adding a quest at " +
					getPosAsLogString(geometry.center) +
					" because there is a note at that position");
			return false;
		}
		return true;
	}

	private static String getElementAsLogString(Element element)
	{
		return element.getType().name().toLowerCase() + " #" + element.getId();
	}

	private static String getQuestTypeAsLogString(QuestType q)
	{
		return q.getClass().getSimpleName();
	}

	private static String getPosAsLogString(LatLon pos)
	{
		return pos.getLatitude() + ", " + pos.getLongitude();
	}
}
