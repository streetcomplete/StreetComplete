package de.westnordost.streetcomplete.data.osm.persist;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

public class MergedElementDao
{
	private final NodeDao nodeDao;
	private final WayDao wayDao;
	private final RelationDao relationDao;

	@Inject
	public MergedElementDao(NodeDao nodeDao, WayDao wayDao, RelationDao relationDao)
	{
		this.nodeDao = nodeDao;
		this.wayDao = wayDao;
		this.relationDao = relationDao;
	}

	public void putAll(Collection<Element> elements)
	{
		Collection<Node> nodes = new ArrayList<>();
		Collection<Way> ways = new ArrayList<>();
		Collection<Relation> relations = new ArrayList<>();

		for(Element element : elements)
		{
			switch(element.getType())
			{
				case NODE:
					nodes.add((Node) element);
					break;
				case WAY:
					ways.add((Way) element);
					break;
				case RELATION:
					relations.add((Relation) element);
					break;
			}
		}
		if(!nodes.isEmpty()) nodeDao.putAll(nodes);
		if(!ways.isEmpty()) wayDao.putAll(ways);
		if(!relations.isEmpty()) relationDao.putAll(relations);
	}

	public void put(Element element)
	{
		switch(element.getType())
		{
			case NODE:
				nodeDao.put((Node) element);
				break;
			case WAY:
				wayDao.put((Way) element);
				break;
			case RELATION:
				relationDao.put((Relation) element);
				break;
		}
	}

	public void delete(Element.Type type, long id)
	{
		switch(type)
		{
			case NODE:
				nodeDao.delete(id);
				break;
			case WAY:
				wayDao.delete(id);
				break;
			case RELATION:
				relationDao.delete(id);
				break;
		}
	}

	public Element get(Element.Type type, long id)
	{
		switch(type)
		{
			case NODE:
				return nodeDao.get(id);
			case WAY:
				return wayDao.get(id);
			case RELATION:
				return relationDao.get(id);
		}
		return null;
	}

	public void deleteUnreferenced()
	{
		nodeDao.deleteUnreferenced();
		wayDao.deleteUnreferenced();
		relationDao.deleteUnreferenced();
	}
}
