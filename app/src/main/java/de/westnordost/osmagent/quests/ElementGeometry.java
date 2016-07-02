package de.westnordost.osmagent.quests;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import de.westnordost.osmapi.map.data.LatLon;

/** Information on the geometry of a quest */
public class ElementGeometry implements Serializable
{
	private List<List<LatLon>> outer;
	private List<List<LatLon>> inner;

	public ElementGeometry(LatLon position)
	{
		outer = Collections.singletonList(Collections.singletonList(position));
		inner = null;
	}

	public ElementGeometry(List<LatLon> positions)
	{
		outer = Collections.singletonList(positions);
		inner = null;
	}

	public ElementGeometry(List<List<LatLon>> outer, List<List<LatLon>> inner)
	{
		this.outer = outer;
		this.inner = inner;
	}


	// TODO add tests in CreateQuestMapDataHandlerTest
}
