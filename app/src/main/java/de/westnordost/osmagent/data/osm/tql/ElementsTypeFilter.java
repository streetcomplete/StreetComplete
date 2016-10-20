package de.westnordost.osmagent.data.osm.tql;

/** Enum that specifies which type(s) of elements to retrieve */
public enum ElementsTypeFilter
{
	NODES("nodes", "node"),
	WAYS("ways", "way"),
	RELATIONS("relations", "rel"),
	/** nodes, ways and relations */
	ELEMENTS("elements", null);

	ElementsTypeFilter(String name, String oqlName)
	{
		this.name = name;
		this.oqlName = oqlName;
	}

	public static ElementsTypeFilter[] OQL_VALUES = {NODES, WAYS, RELATIONS};

	final String name;
	final String oqlName;
}
