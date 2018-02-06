package de.westnordost.streetcomplete.data.osm.download;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.westnordost.osmapi.ApiResponseReader;

public class OverpassStatusParser implements ApiResponseReader<OverpassStatus>
{
	private final Pattern
			maxAvailableSlots = Pattern.compile("Rate limit: (\\d+)"),
			availableSlotsPattern = Pattern.compile("(\\d+) slots available now"),
			nextAvailableSlotPattern = Pattern.compile("Slot available after: ([0-9A-Z-:]+), in (\\d+) seconds");

	@Override public OverpassStatus parse(InputStream in) throws Exception
	{
		OverpassStatus result = new OverpassStatus();

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8")))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				Matcher m;

				m = availableSlotsPattern.matcher(line);
				if (m.find()) result.availableSlots = Integer.parseInt(m.group(1));

				m = nextAvailableSlotPattern.matcher(line);
				if (m.find())
				{
					int nextAvailableSlotIn = Integer.parseInt(m.group(2));
					// Overpass may send several of those lines, actually. Lets take the one that takes least as long
					if (result.nextAvailableSlotIn == null || result.nextAvailableSlotIn > nextAvailableSlotIn)
					{
						result.nextAvailableSlotIn = nextAvailableSlotIn;
					}
				}

				m = maxAvailableSlots.matcher(line);
				if (m.find()) result.maxAvailableSlots = Integer.parseInt(m.group(1));
			}
		}
		return result;
	}
}
