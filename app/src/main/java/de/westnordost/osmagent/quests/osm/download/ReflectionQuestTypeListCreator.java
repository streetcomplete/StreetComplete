package de.westnordost.osmagent.quests.osm.download;

import android.support.annotation.NonNull;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.westnordost.osmagent.quests.QuestType;

/** Creates a list of all quest types in the given package via reflection, sorted by importance */
public class ReflectionQuestTypeListCreator
{
	@NonNull public static <T extends QuestType> List<T> create(Class<T> tClass, String questPackage)
	{
		List<T> questTypeList = new ArrayList<>();
		Configuration reflectionsConf = new ConfigurationBuilder()
				.filterInputsBy(new FilterBuilder().includePackage(questPackage))
				.setUrls(ClasspathHelper.forPackage(questPackage))
				.setScanners(new SubTypesScanner());
		Reflections reflections = new Reflections(reflectionsConf);
		Set<Class<? extends T>> questClasses = getAllSubtypesOf(reflections, tClass);

		for(Class<? extends T> questClass : questClasses)
		{
			if(Modifier.isAbstract(questClass.getModifiers()))
			{
				continue;
			}

			try
			{
				T q = questClass.newInstance();
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

	private static <T extends QuestType> Set<Class<? extends T>> getAllSubtypesOf(
			Reflections reflections, Class<T> tClass)
	{
		Set<Class<? extends T>> result = new HashSet<>();
		Set<Class<? extends T>> questClasses = reflections.getSubTypesOf(tClass);

		result.addAll(questClasses);
		for(Class<? extends T> questClass : questClasses)
		{
			result.addAll(getAllSubtypesOf(reflections, questClass));
		}
		return result;
	}

	private static class QuestImportanceComparator implements Comparator<QuestType>
	{
		@Override public int compare(QuestType lhs, QuestType rhs)
		{
			return lhs.importance() - rhs.importance();
		}
	}
}
