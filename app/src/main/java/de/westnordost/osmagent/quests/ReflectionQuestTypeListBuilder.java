package de.westnordost.osmagent.quests;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import de.westnordost.osmagent.quests.types.QuestType;

/** Creates a list of all quest types, sorted by importance */
public class ReflectionQuestTypeListBuilder
{
	private static final String questPackage = "de.westnordost.osmagent.quests.types";

	public List<QuestType> build()
	{
		List<QuestType> questTypeList = new ArrayList<>();
		Configuration reflectionsConf = new ConfigurationBuilder().
				filterInputsBy(new FilterBuilder().includePackage(questPackage)).
				setUrls(ClasspathHelper.forPackage(questPackage)).
				setScanners(new SubTypesScanner());
		Reflections reflections = new Reflections(reflectionsConf);
		Set<Class<? extends QuestType>> questClasses = reflections.getSubTypesOf(QuestType.class);
		for(Class<? extends QuestType> questClass : questClasses)
		{
			try
			{
				QuestType q = questClass.newInstance();
				questTypeList.add(q);
			}
			catch (InstantiationException e)
			{
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
		// sort by importance
		Collections.sort(questTypeList, new QuestImportanceComparator());

		return questTypeList;
	}

	private class QuestImportanceComparator implements Comparator<QuestType>
	{
		@Override
		public int compare(QuestType lhs, QuestType rhs)
		{
			return lhs.importance() - rhs.importance();
		}
	}
}
