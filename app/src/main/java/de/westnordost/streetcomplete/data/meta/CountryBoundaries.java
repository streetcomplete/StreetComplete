package de.westnordost.streetcomplete.data.meta;

import android.content.res.AssetManager;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.io.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;

public class CountryBoundaries
{
	private final AssetManager assetManager;

	@Inject public CountryBoundaries(AssetManager assetManager)
	{
		this.assetManager = assetManager;
	}


	private void x()
	{
		Quadtree quadtree = new Quadtree();

		GeometryCollection countryBoundaries = (GeometryCollection) loadFromFile("countryBoundaries.geojson");
		for(int i = 0; i < countryBoundaries.getNumGeometries(); ++i)
		{
			Geometry countryBoundary = countryBoundaries.getGeometryN(i);
			quadtree.insert(countryBoundary.getEnvelopeInternal(), countryBoundary);
		}
	}

	private Geometry loadFromFile(String filename) throws IOException, ParseException
	{
		InputStream is = null;
		try
		{
			is = assetManager.open(filename);
			Reader reader =  new InputStreamReader(is, "UTF-8");
			GeoJsonReader geoJsonReader = new GeoJsonReader();
			return geoJsonReader.read(reader);
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
