package de.westnordost.streetcomplete.data.osm.tql;

/** Enum that specifies which type(s) of elements to retrieve */
public enum ElementsTypeFilter
{
	NODES("nodes", "node"),
	WAYS("ways", "way"),
	RELATIONS("relations", "rel");

	ElementsTypeFilter(String name, String oqlName)
	{
		this.name = name;
		this.oqlName = oqlName;
	}

	final String name;
	final String oqlName;
}
