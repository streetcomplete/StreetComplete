package de.westnordost.osmagent.quests.osm.persist;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;

public class MergedElementDao
{
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;

	@Inject
	public MergedElementDao(NodeDao nodeDao, WayDao wayDao, RelationDao relationDao)
	{
		this.nodeDao = nodeDao;
		this.wayDao = wayDao;
		this.relationDao = relationDao;
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
