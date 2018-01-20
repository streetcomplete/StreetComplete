package de.westnordost.streetcomplete.data.complete;

import java.util.Date;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.data.Quest;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;

public class CompleteQuest implements Quest
{
	public CompleteQuest(Complete complete, CompleteQuestType completeQuestType, Element.Type elementType, long elementId,
					ElementGeometry geometry)
	{
		this(null, complete, QuestStatus.NEW, new Date(), completeQuestType, elementType, elementId, geometry);
	}

	public CompleteQuest(Long id, Complete complete, QuestStatus status,
						 Date lastUpdate, CompleteQuestType questType,
						 Element.Type elementType, long elementId, ElementGeometry geometry)
	{
		this.id = id;
		this.status = status;
		this.lastUpdate = lastUpdate;
		this.questType = questType;
		this.complete = complete;
		this.elementType = elementType;
		this.elementId = elementId;
		this.geometry = geometry;
	}

	private Long id;
	private Date lastUpdate;
	private QuestStatus status;
	private Complete complete;

	private final CompleteQuestType questType;

	private final ElementGeometry geometry;

	// underlying OSM data
	private final Element.Type elementType;
	private final long elementId;

	public Complete getComplete()
	{
		return complete;
	}
	public void setComplete(Complete complete)
	{
		this.complete = complete;
	}

	public void setAnswer(String answer) { complete.answer = answer; }

	@Override public QuestType getType() { return questType; }

	@Override public QuestStatus getStatus() { return status; }
	@Override public void setStatus(QuestStatus status) { this.status = status; }

	@Override public Long getId() { return id; }
	@Override public void setId(long id) { this.id = id; }

	@Override public LatLon getMarkerLocation()
	{
		return geometry.center;
	}
	@Override public ElementGeometry getGeometry()
	{
		return geometry;
	}

	public long getElementId()
	{
		return elementId;
	}

	public Element.Type getElementType()
	{
		return elementType;
	}

	@Override public Date getLastUpdate() { return lastUpdate; }

}
