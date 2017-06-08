package de.westnordost.streetcomplete.data.changesets;

/** A pkey in the OpenChangeset table */
public class OpenChangesetKey
{
	public final String questType;
	public final String source;

	public OpenChangesetKey(String questType, String source)
	{
		this.questType = questType;
		this.source = source;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OpenChangesetKey that = (OpenChangesetKey) o;
		return questType.equals(that.questType) &&  source.equals(that.source);
	}

	@Override public int hashCode()
	{
		return 31 * questType.hashCode() + source.hashCode();
	}
}
