package de.westnordost.streetcomplete.data.meta;

import android.test.AndroidTestCase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CountryBoundariesGeoJsonTest extends AndroidTestCase
{
	public void testCountryBoundariesGeoJson() throws IOException
	{
		String geoJson = readToString(getContext().getAssets().open("countryBoundaries.json"));
		GeometryCollection countriesBoundaries = (GeometryCollection) new GeoJsonReader().read(geoJson);
		for(int i = 0; i < countriesBoundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = countriesBoundaries.getGeometryN(i);
			Map<String,String> props = (Map<String,String>) countryBoundary.getUserData();

			TopologyValidationError err = new IsValidOp(countryBoundary).getValidationError();
			if(err != null) {
				String countryCode = props.get("ISO3166-1:alpha2");
				if(countryCode == null) countryCode = props.get("ISO3166-2");
				fail("" + countryCode + ": " + err.toString());
			}
		}
	}

	private String readToString(InputStream is) throws IOException
	{
		try
		{
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) != -1)
			{
				result.write(buffer, 0, length);
			}
			return result.toString("UTF-8");
		}
		finally
		{
			if(is != null) try
			{
				is.close();
			}
			catch (IOException e) { }
		}
	}
}
