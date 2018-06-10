package de.westnordost.streetcomplete.data.osm;

import android.support.annotation.NonNull;

import java.util.Collections;

/** Some defaults for OsmElementQuestType interface */
public abstract class AOsmElementQuestType implements OsmElementQuestType
{
	@Override public int getDefaultDisabledMessage() { return 0; }
	@NonNull @Override public Countries getEnabledForCountries() { return Countries.ALL; }
	@Override public void cleanMetadata() { }
	@Override public int getTitle() { return getTitle(Collections.emptyMap()); }
}
