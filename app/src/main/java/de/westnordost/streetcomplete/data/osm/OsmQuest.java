package de.westnordost.streetcomplete.data.osm;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;

import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

/** Represents one task for the user to complete/correct the data based on one OSM element */
public class OsmQuest implements Quest
{
	public OsmQuest(OsmElementQuestType type, Element.Type elementType, long elementId,
					ElementGeometry geometry)
	{
		this(null, type, elementType, elementId, QuestStatus.NEW, null, null, new Date(), geometry);
	}

	public OsmQuest(Long id, OsmElementQuestType type, Element.Type elementType, long elementId,
					QuestStatus status, @Nullable StringMapChanges changes,
					@Nullable  String changesSource, Date lastUpdate, ElementGeometry geometry)
	{
		this.id = id;
		this.type = type;
		this.elementType = elementType;
		this.elementId = elementId;
		this.changes = changes;
		this.changesSource = changesSource;
		this.geometry = geometry;
		this.status = status;
		this.lastUpdate = lastUpdate;
	}

	private Long id;
	private final OsmElementQuestType type;
	private QuestStatus status;
	private final ElementGeometry geometry;

	// underlying OSM data
	private final Element.Type elementType;
	private final long elementId;

	// and the changes to the tags (in the future, streetcomplete should probably be able to edit more
	// than just tags -> osmchange?)
	private StringMapChanges changes;
	private String changesSource;

	private Date lastUpdate;

	@Override public Long getId()
	{
		return id;
	}

	@Override public LatLon[] getMarkerLocations()
	{
		if(getOsmElementQuestType().getHasMarkersAtEnds() && geometry.polylines != null)
		{
			List<LatLon> polyline = geometry.polylines.get(0);
			double length = SphericalEarthMath.distance(polyline);
			if(length > 15*4)
			{
				return new LatLon[]{
					SphericalEarthMath.pointOnPolylineFromStart(polyline, 15),
					SphericalEarthMath.pointOnPolylineFromEnd(polyline, 15),
				};
			}
		}
		return new LatLon[]{getCenter()};
	}

	@Override public LatLon getCenter()
	{
		return geometry.center;
	}

	public long getElementId()
	{
		return elementId;
	}

	public Element.Type getElementType()
	{
		return elementType;
	}

	@Override public QuestType getType()
	{
		return type;
	}

	public OsmElementQuestType getOsmElementQuestType()
	{
		return type;
	}

	@Override public ElementGeometry getGeometry()
	{
		return geometry;
	}

	public StringMapChanges getChanges()
	{
		return changes;
	}

	public String getChangesSource() { return changesSource; }

	public void setChanges(StringMapChanges changes, String source)
	{
		this.changes = changes;
		this.changesSource = source;
	}

	@Override public QuestStatus getStatus()
	{
		return status;
	}

	@Override public void setStatus(QuestStatus status)
	{
		this.status = status;
	}

	@Override public Date getLastUpdate()
	{
		return lastUpdate;
	}

	@Override public void setId(long id)
	{
		this.id = id;
	}
}
