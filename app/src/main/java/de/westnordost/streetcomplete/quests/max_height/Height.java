package de.westnordost.streetcomplete.quests.max_height;

//if the minimum required API would be 24, we could maybe use https://developer.android.com/reference/android/icu/util/Measure
public class Height
{
	private int integerPart;
	private int fractionalPart;

	private Unit unit;

	public enum Unit {
		METRIC,
		IMPERIAL
	}

	Height()
	{
		this.integerPart = -1;
		this.fractionalPart = -1;
		this.unit = Unit.METRIC;
	}

	Height(String integerPart, String fractionalPart, Unit unit)
	{
		this.integerPart = Integer.parseInt(integerPart);
		this.fractionalPart = Integer.parseInt(fractionalPart);
		this.unit = unit;
	}

	public Unit getUnit()
	{
		return unit;
	}

	public boolean isEmpty()
	{
		return integerPart == -1 && fractionalPart == -1;
	}

	public String toString()
	{
		if (unit.equals(Unit.METRIC))
		{
			return String.valueOf(integerPart) + "." + String.valueOf(fractionalPart);
		}
		else
		{
			//this adds an apostrophe and a double-quote to be in a format like e.g. 6'7"
			return String.valueOf(integerPart) + "'" + String.valueOf(fractionalPart) + "\"";
		}
	}

	public double toDouble()
	{
		return Double.parseDouble(integerPart + "." + fractionalPart);
	}
}
