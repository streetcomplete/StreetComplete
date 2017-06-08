package de.westnordost.streetcomplete.data.changesets;

/** A row in the OpenChangeset table */
public class OpenChangesetInfo
{
	public final OpenChangesetKey key;
	public final Long changesetId;

	public OpenChangesetInfo(OpenChangesetKey key, Long changesetId)
	{
		this.key = key;
		this.changesetId = changesetId;
	}
}
