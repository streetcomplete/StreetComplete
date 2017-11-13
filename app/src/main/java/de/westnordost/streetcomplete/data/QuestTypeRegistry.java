package de.westnordost.streetcomplete.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Every osm quest needs to be registered here.
 *
 *  Could theoretically be done with Reflection, but that doesn't really work on Android
 */

public class QuestTypeRegistry
{
	private final List<QuestType> types;
	private final Map<String, QuestType> typeMap;

	public QuestTypeRegistry(List<QuestType> types)
	{
		this.types = types;

		typeMap = new HashMap<>();
		for (QuestType questType : types)
		{
			String questTypeName = questType.getClass().getSimpleName();
			if(typeMap.containsKey(questTypeName))
			{
				throw new RuntimeException("A quest type's name must be unique! \"" +
						questTypeName + "\" is defined twice!");
			}
			typeMap.put(questTypeName, questType);
		}
	}

	public QuestType getByName(String typeName)
	{
		return typeMap.get(typeName);
	}

	public List<QuestType> getAll()
	{
		return Collections.unmodifiableList(types);
	}
}
