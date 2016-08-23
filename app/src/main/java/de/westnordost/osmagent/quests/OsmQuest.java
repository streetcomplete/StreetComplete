package de.westnordost.osmagent.quests;

import javax.annotation.Nullable;

import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.map.data.Element;

/** Represents one task for the user to complete/correct the data based on one OSM element */
public class OsmQuest implements Quest
{
	public OsmQuest(long id, QuestType type, long elementId, Element.Type elementType,
					QuestStatus status,	@Nullable StringMapChanges changes, ElementGeometry geometry)
	{
		this.id = id;
		this.type = type;
		this.elementId = elementId;
		this.elementType = elementType;
		this.changes = changes;
		this.geometry = geometry;
		this.status = status;
	}

	private long id;
	private final QuestType type;
	private QuestStatus status;
	private final ElementGeometry geometry;

	// underlying OSM data
	private final long elementId;
	private final Element.Type elementType;

	// and the changes to the tags (in the future, osmagent should probably be able to edit more
	// than just tags -> osmchange?)
	private StringMapChanges changes;

	public long getId()
	{
		return id;
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

	public ElementGeometry getGeometry()
	{
		return geometry;
	}

	public StringMapChanges getChanges()
	{
		return changes;
	}

	public void setChanges(StringMapChanges changes)
	{
		this.changes = changes;
	}

	@Override public QuestStatus getStatus()
	{
		return status;
	}

	@Override public void setStatus(QuestStatus status)
	{
		this.status = status;
	}
}
