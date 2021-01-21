package de.westnordost.streetcomplete.data.download;

import java.util.Arrays;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader;
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementDownloader;
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesDownloader;

@Module
public class DownloadModule2
{
	/* NOTE: For some reason, when converting this to Kotlin, Dagger 2 does not find this anymore
	*  and cannot provide the dependency for UploadService. So, it must stay in Java (for now) */
	@Provides public static List<? extends Downloader> downloaders(
		OsmNotesDownloader osmNotesDownloader,
		OsmElementDownloader osmElementDownloader,
		MapTilesDownloader mapTilesDownloader
	) {
		return Arrays.asList(osmNotesDownloader, osmElementDownloader, mapTilesDownloader);
	}
}
