package de.westnordost.streetcomplete.data.osm.download;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmElementKey;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;

// TODO test case
public class OsmQuestDownload
{
	private static final String TAG = "QuestDownload";

	// injections
	private final ElementGeometryDao geometryDB;
	private final MergedElementDao elementDB;
	private final OsmQuestDao osmQuestDB;
	private final DownloadedTilesDao downloadedTilesDao;

	// listener
	private VisibleQuestListener questListener;

	@Inject public OsmQuestDownload(
			ElementGeometryDao geometryDB,
			MergedElementDao elementDB, OsmQuestDao osmQuestDB,
			DownloadedTilesDao downloadedTilesDao)
	{
		this.geometryDB = geometryDB;
		this.elementDB = elementDB;
		this.osmQuestDB = osmQuestDB;
		this.downloadedTilesDao = downloadedTilesDao;
	}

	public void setQuestListener(VisibleQuestListener listener)
	{
		this.questListener = listener;
	}

	public int download(final OsmElementQuestType questType, Rect tiles,
						  final Set<LatLon> blacklistedPositions)
	{
		BoundingBox bbox = SlippyMapMath.asBoundingBox(tiles, ApplicationConstants.QUEST_TILE_ZOOM);

		final ArrayList<ElementGeometryDao.Row> geometryRows = new ArrayList<>();
		final Map<OsmElementKey,Element> elements = new HashMap<>();
		final ArrayList<OsmQuest> quests = new ArrayList<>();
		final Map<OsmElementKey, Long> previousQuests = getPreviousQuestsIdsByElementKey(questType, bbox);

		boolean success = questType.download(bbox, new MapDataWithGeometryHandler()
		{
			@Override public void handle(@NonNull Element element, @Nullable ElementGeometry geometry)
			{
				if(mayCreateQuestFrom(questType, element, geometry, blacklistedPositions))
				{
					Element.Type elementType = element.getType();
					long elementId = element.getId();

					OsmQuest quest = new OsmQuest(questType, elementType, elementId, geometry);

					geometryRows.add(new ElementGeometryDao.Row(
							elementType, elementId, quest.getGeometry()));
					quests.add(quest);
					OsmElementKey elementKey = new OsmElementKey(elementType, elementId);
					elements.put(elementKey, element);
					previousQuests.remove(elementKey);
				}
			}
		});
		if(!success) return 0;

		// geometry and elements must be put into DB first because quests have foreign keys on it
		geometryDB.putAll(geometryRows);
		elementDB.putAll(elements.values());

		int newQuestsByQuestType = osmQuestDB.addAll(quests);

		if(questListener != null && !quests.isEmpty())
		{
			Iterator<OsmQuest> it = quests.iterator();
			while(it.hasNext())
			{
				// it is null if this quest is already in the DB, so don't call onQuestCreated
				if(it.next().getId() == null) it.remove();
			}
			questListener.onQuestsCreated(quests, QuestGroup.OSM);
		}

		if(!previousQuests.isEmpty())
		{
			if(questListener != null)
			{
				questListener.onQuestsRemoved(previousQuests.values(), QuestGroup.OSM);
			}

			osmQuestDB.deleteAll(previousQuests.values());
		}

		// note: this could be done after ALL osm quest types have been downloaded if this
		// turns out to be slow if done for every quest type
		geometryDB.deleteUnreferenced();
		elementDB.deleteUnreferenced();

		downloadedTilesDao.put(tiles, getQuestTypeName(questType));

		int visibleQuestsByQuestType = quests.size();
		int obsoleteAmount = previousQuests.size();
		Log.i(TAG, getQuestTypeName(questType) + ": " +
				"Added " + newQuestsByQuestType + " new and " +
				"removed " + obsoleteAmount + " already resolved quests." +
				" (Total: " + visibleQuestsByQuestType + ")");

		return visibleQuestsByQuestType;
	}

	private Map<OsmElementKey, Long> getPreviousQuestsIdsByElementKey(
			OsmElementQuestType questType, BoundingBox bbox)
	{
		String questTypeName = questType.getClass().getSimpleName();
		Map<OsmElementKey, Long> result = new HashMap<>();
		for(OsmQuest quest : osmQuestDB.getAll(bbox, null, questTypeName, null, null))
		{
			result.put(new OsmElementKey(quest.getElementType(), quest.getElementId()),	quest.getId());
		}
		return result;
	}

	private boolean mayCreateQuestFrom(OsmElementQuestType questType, Element element,
									   ElementGeometry geometry, Set<LatLon> blacklistedPositions)
	{
		// invalid geometry -> can't show this quest, so skip it
		if(geometry == null)
		{
			// classified as warning because it might very well be a bug on the geometry
			// creation on our side
			Log.w(TAG, getQuestTypeName(questType) + ": Not adding a quest " +
					" because the element " + getElementAsLogString(element) +
					" has no valid geometry");
			return false;
		}

		// do not create quests whose marker is at a blacklisted position
		if(blacklistedPositions != null && blacklistedPositions.contains(geometry.center))
		{
			Log.d(TAG, getQuestTypeName(questType) + ": Not adding a quest at " +
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

	private static String getQuestTypeName(QuestType q)
	{
		return q.getClass().getSimpleName();
	}

	private static String getPosAsLogString(LatLon pos)
	{
		return pos.getLatitude() + ", " + pos.getLongitude();
	}
}
