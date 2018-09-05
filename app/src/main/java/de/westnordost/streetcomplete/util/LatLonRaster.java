package de.westnordost.streetcomplete.util;

import java.util.ArrayList;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;

/** A spatial index implemented as a grid, based on points */
public class LatLonRaster
{
	private final ArrayList<LatLon>[] raster;
	private final int rasterWidth;
	private final int rasterHeight;
	private final double cellSize;
	private final BoundingBox bbox;
	private int size;

	public LatLonRaster(BoundingBox bounds, double cellSize)
	{
		double lonDiff = SphericalEarthMath.normalizeLongitude(bounds.getMaxLongitude() - bounds.getMinLongitude());
		double latDiff = bounds.getMaxLatitude() - bounds.getMinLatitude();
		rasterWidth = (int) Math.ceil(lonDiff / cellSize);
		rasterHeight = (int) Math.ceil(latDiff / cellSize);
		raster = new ArrayList[rasterWidth * rasterHeight];

		double maxLon = SphericalEarthMath.normalizeLongitude(bounds.getMinLongitude() + rasterWidth * cellSize);
		double maxLat = bounds.getMinLatitude() + rasterHeight * cellSize;
		this.bbox = new BoundingBox(bounds.getMin(), new OsmLatLon(maxLat, maxLon));
		this.cellSize = cellSize;
	}

	public void insert(LatLon p)
	{
		int x = longitudeToCellX(p.getLongitude());
		int y = latitudeToCellY(p.getLatitude());
		checkBounds(x, y);

		ArrayList<LatLon> list = raster[y * rasterWidth + x];
		if(list == null)
		{
			list = new ArrayList<>();
			raster[y * rasterWidth + x] = list;
		}
		list.add(p);
		size++;
	}

	public Iterable<LatLon> getAll(BoundingBox bounds)
	{
		int startX = Math.max(0, Math.min(longitudeToCellX(bounds.getMinLongitude()), rasterWidth-1));
		int startY = Math.max(0, Math.min(latitudeToCellY(bounds.getMinLatitude()), rasterHeight-1));
		int endX = Math.max(0, Math.min(longitudeToCellX(bounds.getMaxLongitude()), rasterWidth-1));
		int endY = Math.max(0, Math.min(latitudeToCellY(bounds.getMaxLatitude()), rasterHeight-1));

		MultiIterable<LatLon> result = new MultiIterable<>();
		for (int y = startY; y <= endY; y++)
		{
			for (int x = startX; x <= endX; x++)
			{
				ArrayList<LatLon> list = raster[y * rasterWidth + x];
				if(list != null) result.add(list);
			}
		}
		return result;
	}

	public boolean remove(LatLon p)
	{
		int x = longitudeToCellX(p.getLongitude());
		int y = latitudeToCellY(p.getLatitude());
		if(x < 0 || x >= rasterWidth || y < 0 || y >= rasterHeight) return false;

		ArrayList<LatLon> list = raster[y * rasterWidth + x];
		if(list == null) return false;
		boolean result = list.remove(p);
		if(result) --size;
		return result;
	}

	public int size()
	{
		return size;
	}

	private void checkBounds(int x, int y)
	{
		if(x < 0 || x >= rasterWidth)
			throw new IllegalArgumentException("Longitude is out of bounds");
		if(y < 0 || y >= rasterHeight)
			throw new IllegalArgumentException("Latitude is out of bounds");
	}

	private int longitudeToCellX(double longitude)
	{
		return (int) Math.floor(SphericalEarthMath.normalizeLongitude(longitude - bbox.getMinLongitude()) / cellSize);
	}

	private int latitudeToCellY(double latitude)
	{
		return (int) Math.floor((latitude - bbox.getMinLatitude()) / cellSize);
	}
}
