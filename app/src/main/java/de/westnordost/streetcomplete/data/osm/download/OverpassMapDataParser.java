package de.westnordost.streetcomplete.data.osm.download;

import android.util.LongSparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.common.OsmXmlDateFormat;
import de.westnordost.osmapi.common.XmlParser;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;
import de.westnordost.osmapi.map.data.Way;

/** A map data parser that also parses the geometry of elements. (Overpass parameter "geom")*/
public class OverpassMapDataParser extends XmlParser implements ApiResponseReader<Void>, WayGeometrySource
{
	private static final String
			NODE = "node",
			WAY = "way",
			RELATION = "relation",
			MEMBER = "member",
			ND = "nd",
			TAG = "tag";

	private final OsmXmlDateFormat dateFormat = new OsmXmlDateFormat();

	private final ElementGeometryCreator elementGeometryCreator;
	private final MapDataFactory factory;

	private MapDataWithGeometryHandler handler;

	private long id;
	private int version;

	private Double lat;
	private Double lon;

	private Map<String, String> tags;
	private List<RelationMember> members;
	private List<Long> nodes;

	private LongSparseArray<List<LatLon>> nodePositionsByWay;

	private List<LatLon> wayNodes;

	public OverpassMapDataParser(
			ElementGeometryCreator elementGeometryCreator,
			MapDataFactory factory)
	{
		this.factory = factory;
		this.elementGeometryCreator = elementGeometryCreator;
		this.elementGeometryCreator.setWayGeometryProvider(this);
	}

	void setHandler(MapDataWithGeometryHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public Void parse(InputStream in) throws IOException
	{
		if(handler == null) throw new NullPointerException();

		id = -1;
		version = 0;

		doParse(in);

		return null;
	}

	@Override
	protected void onStartElement()
	{
		String name = getName();

		switch (name)
		{
			case TAG:
				if (tags == null)
				{
					tags = new HashMap<>();
				}
				tags.put(getAttribute("k"), getAttribute("v"));
				break;

			case ND:
				Long ndRef = getLongAttribute("ref");
				if(ndRef != null) // null for ND nodes in MEMBER
				{
					nodes.add(ndRef);
				}

				LatLon pos = new OsmLatLon(getDoubleAttribute("lat"), getDoubleAttribute("lon"));
				wayNodes.add(pos);
				break;

			case MEMBER:
				long ref = getLongAttribute("ref");
				String role = getAttribute("role");
				Element.Type type = Element.Type.valueOf(getAttribute("type").toUpperCase(Locale.UK));
				members.add(factory.createRelationMember(ref, role, type));
				startWayGeometry(ref);
				break;

			case NODE:
				retrieveIdAndVersion();
				lat = getDoubleAttribute("lat");
				lon = getDoubleAttribute("lon");
				break;

			case WAY:
				retrieveIdAndVersion();
				nodes = new ArrayList<>();
				nodePositionsByWay = new LongSparseArray<>();
				startWayGeometry(id);
				break;

			case RELATION:
				retrieveIdAndVersion();
				members = new ArrayList<>();
				nodePositionsByWay = new LongSparseArray<>();
				break;
		}
	}

	private void retrieveIdAndVersion()
	{
		id = getLongAttribute("id");
		// for when output mode "out skel;"
		Integer version = getIntAttribute("version");
		this.version = version != null ? version : -1;
	}

	private void startWayGeometry(long wayId)
	{
		wayNodes = new ArrayList<>();
		nodePositionsByWay.put(wayId, wayNodes);
	}

	@Override
	protected void onEndElement()
	{
		String name = getName();

		Element element = null;
		ElementGeometry geometry = null;

		switch(name)
		{
			case MEMBER:
				wayNodes = null;
				break;

			case NODE:
				Node node = factory.createNode(id, version, lat, lon, tags, null, null);
				geometry = elementGeometryCreator.create(node);
				element = node;
				break;

			case WAY:
				Way way = factory.createWay(id, version, nodes, tags, null, null);
				geometry = elementGeometryCreator.create(way);
				element = way;
				nodes = null;
				nodePositionsByWay = null;
				wayNodes = null;
				break;

			case RELATION:
				Relation relation = factory.createRelation(id, version, members, tags, null, null);
				geometry = elementGeometryCreator.create(relation);
				element = relation;
				members = null;
				nodePositionsByWay = null;
				break;
		}

		if(element != null)
		{
			tags = null;
			handler.handle(element, geometry);
		}
	}

	@Override public List<LatLon> getNodePositions(long wayId)
	{
		return nodePositionsByWay.get(wayId);
	}
}
