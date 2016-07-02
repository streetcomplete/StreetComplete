package de.westnordost.osmagent.quests;

import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.map.data.Element;

/** Represents one task for the user to complete/correct the data */
public class Quest
{
	public Quest(QuestType type, Element element, ElementGeometry geometry)
	{
		this.type = type;
		this.element = element;
		this.geometry = geometry;
	}

	private final Element element;
	private final QuestType type;
	private final ElementGeometry geometry;

	public Element getElement()
	{
		return element;
	}

	public QuestType getType()
	{
		return type;
	}

	public ElementGeometry getGeometry()
	{
		return geometry;
	}
}
