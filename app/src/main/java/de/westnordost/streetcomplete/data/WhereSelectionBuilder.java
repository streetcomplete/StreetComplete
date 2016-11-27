package de.westnordost.streetcomplete.data;

import java.util.ArrayList;
import java.util.Arrays;

public class WhereSelectionBuilder
{
	StringBuilder where = new StringBuilder();
	ArrayList<String> whereArgs = new ArrayList<>();

	public WhereSelectionBuilder() {}

	public void appendAnd(String clause, String... args)
	{
		if(where.length() > 0) where.append(" AND ");
		where.append(clause);
		this.whereArgs.addAll(Arrays.asList(args));
	}

	public String[] getArgs()
	{
		return whereArgs.toArray(new String[whereArgs.size()]);
	}

	public String getWhere()
	{
		return where.toString();
	}

}
